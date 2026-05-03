package com.example.music.controller;

import com.example.music.dto.CommentDTO;
import com.example.music.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    void controllerInstantiation() {
        assertNotNull(commentController);
    }

    @Test
    void commentDTO_CreatedCorrectly() {
        CommentDTO dto = CommentDTO.builder()
                .id(1L).content("Great song!").userId(1L).userName("Test User")
                .songId(1L).build();

        assertEquals(1L, dto.getId());
        assertEquals("Great song!", dto.getContent());
        assertEquals(1L, dto.getUserId());
    }
}
