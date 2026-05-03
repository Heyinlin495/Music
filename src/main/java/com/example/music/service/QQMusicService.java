package com.example.music.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class QQMusicService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String audioQuality;
    private final String qqmusicKey;

    public QQMusicService(RestTemplate restTemplate,
                          @Value("${qqmusic.api.base-url}") String baseUrl,
                          @Value("${qqmusic.audio.quality:128}") String audioQuality,
                          @Value("${qqmusic.api.qqmusic-key:}") String qqmusicKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.audioQuality = audioQuality;
        this.qqmusicKey = qqmusicKey;
    }

    /**
     * Search QQ Music for a song and return the best-matching songmid.
     */
    public String searchAndMatch(String title, String artist) {
        // Try proxy API first
        String result = searchViaProxy(title, artist);
        if (result != null) return result;

        // Fallback: use smartbox API directly
        return searchViaSmartbox(title, artist);
    }

    private String searchViaProxy(String title, String artist) {
        try {
            String query = title + " " + artist;
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = baseUrl + "/search?key=" + encodedQuery + "&pageSize=10";

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !Integer.valueOf(100).equals(response.get("result"))) {
                return null;
            }

            Map data = (Map) response.get("data");
            if (data == null) return null;

            List<Map<String, Object>> songList = (List<Map<String, Object>>) data.get("list");
            if (songList == null || songList.isEmpty()) return null;

            return findBestMatch(songList, title, artist);
        } catch (Exception e) {
            log.warn("Proxy search error for: {} - {}", title, artist, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String searchViaSmartbox(String title, String artist) {
        try {
            String query = title;
            java.net.URI uri = new java.net.URI("https", null, "c.y.qq.com", 443,
                "/splcloud/fcgi-bin/smartbox_new.fcg",
                "key=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&g_tk=5381", null);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Referer", "https://y.qq.com");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map result = response.getBody();
            if (result == null || !Integer.valueOf(0).equals(result.get("code"))) {
                log.warn("Smartbox search failed for: {} - {}", title, artist);
                return null;
            }

            Map data = (Map) result.get("data");
            if (data == null) return null;

            Map songData = (Map) data.get("song");
            if (songData == null) return null;

            List<Map<String, Object>> songList = (List<Map<String, Object>>) songData.get("itemlist");
            if (songList == null || songList.isEmpty()) {
                log.warn("Smartbox no results for: {} - {}", title, artist);
                return null;
            }

            // Smartbox returns: {docid, id, mid, name, singer}
            String bestMid = null;
            int bestScore = 0;
            for (Map<String, Object> song : songList) {
                int score = 0;
                String songName = (String) song.get("name");
                String singerName = (String) song.get("singer");
                String mid = (String) song.get("mid");
                if (songName == null || mid == null) continue;

                if (songName.equalsIgnoreCase(title)) score += 100;
                else if (normalize(songName).equals(normalize(title))) score += 80;
                else if (songName.contains(title) || title.contains(songName)) score += 50;

                if (singerName != null) {
                    if (singerName.equalsIgnoreCase(artist)) score += 100;
                    else if (singerName.contains(artist) || artist.contains(singerName)) score += 50;
                    else if (normalize(singerName).equals(normalize(artist))) score += 80;
                    else if (matchesPartialArtist(singerName, artist)) score += 60;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestMid = mid;
                }
            }

            if (bestScore >= 100 && bestMid != null) {
                log.info("Smartbox matched: {} - {} -> songmid {}", title, artist, bestMid);
                return bestMid;
            }

            log.warn("Smartbox no good match for: {} - {} (best score: {})", title, artist, bestScore);
            return null;
        } catch (Exception e) {
            log.error("Smartbox search error for: {} - {}", title, artist, e);
            return null;
        }
    }

    /**
     * Get the playback URL for a song by its songmid.
     */
    public String getAudioUrl(String songmid) {
        // Try proxy API with qqmusic_key cookie first (VIP account)
        if (qqmusicKey != null && !qqmusicKey.isEmpty()) {
            try {
                String proxyUrl = baseUrl + "/song/url?id=" + songmid + "&type=" + audioQuality;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Cookie", "uin=1770591338; qqmusic_key=" + qqmusicKey + "; qm_keyst=" + qqmusicKey);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(proxyUrl, HttpMethod.GET, entity, Map.class);
                Map result = response.getBody();

                if (result != null && Integer.valueOf(100).equals(result.get("result"))) {
                    String audioUrl = (String) result.get("data");
                    if (audioUrl != null && !audioUrl.isEmpty()) {
                        log.info("Got audio URL from proxy API for songmid: {}", songmid);
                        return audioUrl;
                    }
                }
                log.warn("Proxy API failed for songmid: {}, trying direct API", songmid);
            } catch (Exception e) {
                log.warn("Proxy API error for songmid: {}, trying direct API: {}", songmid, e.getMessage());
            }
        }

        // Fallback: try newer QQ Music API directly
        return getAudioUrlFromNewApi(songmid);
    }

    private String getAudioUrlFromNewApi(String songmid) {
        // Try full song first (needs key), then 30s preview (no key needed)
        String ext = ".mp3";

        // Step 1: Try full song with key (M500 prefix)
        if (qqmusicKey != null && !qqmusicKey.isEmpty()) {
            String result = tryGetAudioUrl(songmid, "M500", "1770591338", ext,
                    "uin=1770591338; qqmusic_key=" + qqmusicKey + "; qm_keyst=" + qqmusicKey);
            if (result != null) return result;
        }

        // Step 2: Fallback to 30s preview without key (RS02 prefix, uin=0)
        String result = tryGetAudioUrl(songmid, "RS02", "0", ext, null);
        if (result != null) {
            log.info("Using 30s preview for songmid {} (full song key expired or unavailable)", songmid);
            return result;
        }

        log.error("All attempts to get audio URL failed for songmid: {}", songmid);
        return null;
    }

    private String tryGetAudioUrl(String songmid, String prefix, String uin, String ext, String cookie) {
        try {
            String filename = prefix + songmid + songmid + ext;
            String guid = String.valueOf((int) (Math.random() * 10000000));

            String dataJson = String.format(
                "{\"req_0\":{\"module\":\"vkey.GetVkeyServer\",\"method\":\"CgiGetVkey\"," +
                "\"param\":{\"filename\":[\"%s\"],\"guid\":\"%s\",\"songmid\":[\"%s\"]," +
                "\"songtype\":[0],\"uin\":\"%s\",\"loginflag\":1,\"platform\":\"20\"}}," +
                "\"comm\":{\"uin\":\"%s\",\"format\":\"json\",\"ct\":19,\"cv\":0}}",
                filename, guid, songmid, uin, uin);

            String urlStr = "https://u.y.qq.com/cgi-bin/musicu.fcg?-=getplaysongvkey&g_tk=5381" +
                "&loginUin=" + uin + "&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8" +
                "&notice=0&platform=yqq.json&needNewCode=0&data=" +
                java.net.URLEncoder.encode(dataJson, java.nio.charset.StandardCharsets.UTF_8);

            java.net.URI uri = new java.net.URI(urlStr);
            log.info("Trying {} for songmid {}", prefix, songmid);

            HttpHeaders headers = new HttpHeaders();
            if (cookie != null) {
                headers.set("Cookie", cookie);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map response = responseEntity.getBody();
            if (response == null) return null;

            if (!response.containsKey("req_0")) {
                log.warn("req_0 NOT found for songmid {} prefix {}", songmid, prefix);
                return null;
            }

            Map req0 = (Map) response.get("req_0");
            if (req0 == null) return null;

            Map reqData = (Map) req0.get("data");
            if (reqData == null) return null;

            java.util.List<String> sip = (java.util.List<String>) reqData.get("sip");
            java.util.List<Map<String, String>> midurlinfo = (java.util.List<Map<String, String>>) reqData.get("midurlinfo");

            if (sip == null || sip.isEmpty() || midurlinfo == null || midurlinfo.isEmpty()) {
                log.warn("sip or midurlinfo empty for songmid {} prefix {}", songmid, prefix);
                return null;
            }

            String purl = midurlinfo.get(0).get("purl");
            if (purl == null || purl.isEmpty()) {
                log.warn("purl empty for songmid {} prefix {} - likely VIP-only or key expired", songmid, prefix);
                return null;
            }
            if (purl.contains("err") || purl.contains("error")) {
                log.warn("purl contains error for songmid {} prefix {}: {}", songmid, prefix, purl);
                return null;
            }

            String domain = sip.stream().filter(s -> !s.startsWith("http://ws")).findFirst().orElse(sip.get(0));
            log.info("Got audio URL for songmid {} prefix {}: domain={}", songmid, prefix, domain);
            return domain + purl;
        } catch (Exception e) {
            log.error("API call failed for songmid {} prefix {}: {}", songmid, prefix, e.getMessage());
            return null;
        }
    }

    /**
     * Get lyrics for a song by its songmid.
     */
    public String getLyrics(String songmid) {
        try {
            String url = baseUrl + "/lyric?songmid=" + songmid;
            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !Integer.valueOf(100).equals(response.get("result"))) {
                return null;
            }

            Map data = (Map) response.get("data");
            if (data == null) return null;

            return (String) data.get("lyric");
        } catch (Exception e) {
            log.error("Error getting lyrics for songmid: {}", songmid, e);
            return null;
        }
    }

    private String findBestMatch(List<Map<String, Object>> songs, String targetTitle, String targetArtist) {
        String bestSongmid = null;
        int bestScore = 0;

        for (Map<String, Object> song : songs) {
            int score = 0;
            String songName = (String) song.get("songname");
            String songmid = (String) song.get("songmid");
            List<Map<String, String>> singers = (List<Map<String, String>>) song.get("singer");
            String singerName = (singers != null && !singers.isEmpty()) ? singers.get(0).get("name") : "";
            Integer interval = (Integer) song.get("interval");

            if (songName == null || songmid == null) continue;

            // Title matching
            if (songName.equalsIgnoreCase(targetTitle)) {
                score += 100;
            } else if (songName.contains(targetTitle) || targetTitle.contains(songName)) {
                score += 50;
            } else if (normalize(songName).equals(normalize(targetTitle))) {
                score += 80;
            }

            // Artist matching
            if (singerName.equalsIgnoreCase(targetArtist)) {
                score += 100;
            } else if (singerName.contains(targetArtist) || targetArtist.contains(singerName)) {
                score += 50;
            } else if (normalize(singerName).equals(normalize(targetArtist))) {
                score += 80;
            } else if (matchesPartialArtist(singerName, targetArtist)) {
                score += 60;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSongmid = songmid;
            }
        }

        // Require minimum score of 100 (at least title OR artist must match well)
        return bestScore >= 100 ? bestSongmid : null;
    }

    private boolean matchesPartialArtist(String singerName, String targetArtist) {
        // Handle cases like "G.E.M.邓紫棋" vs "邓紫棋"
        String normalizedSinger = normalize(singerName);
        String normalizedTarget = normalize(targetArtist);
        return normalizedSinger.contains(normalizedTarget) || normalizedTarget.contains(normalizedSinger);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s.·]", "").toLowerCase();
    }
}
