import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../api';
import { FiEye, FiEyeOff } from 'react-icons/fi';
import './Login.css';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [captchaCode, setCaptchaCode] = useState('');
    const [captchaId, setCaptchaId] = useState('');
    const [captchaImg, setCaptchaImg] = useState('');
    const [captchaError, setCaptchaError] = useState(false);
    const [error, setError] = useState('');
    const { login, loading } = useAuthStore();
    const navigate = useNavigate();

    const fetchCaptcha = useCallback(async () => {
        setCaptchaError(false);
        setCaptchaImg('');
        try {
            const response = await authApi.getCaptcha();
            if (response.data.error) {
                setCaptchaError(true);
                return;
            }
            setCaptchaImg(response.data.image);
            setCaptchaId(response.data.captchaId);
            setCaptchaCode('');
        } catch {
            setCaptchaError(true);
        }
    }, []);

    useEffect(() => {
        fetchCaptcha();
    }, [fetchCaptcha]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const result = await login(username, password, captchaId, captchaCode);
        if (result.success) {
            navigate('/');
        } else {
            setError(result.error);
            fetchCaptcha();
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <h1>管理后台</h1>
                <p className="login-subtitle">音乐平台管理系统</p>

                {error && <div className="error-message">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>用户名</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="请输入管理员用户名"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>密码</label>
                        <div className="password-input-wrapper">
                            <input
                                type={showPassword ? 'text' : 'password'}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="请输入密码"
                                required
                            />
                            <button
                                type="button"
                                className="password-toggle-btn"
                                onClick={() => setShowPassword(!showPassword)}
                                tabIndex={-1}
                            >
                                {showPassword ? <FiEyeOff /> : <FiEye />}
                            </button>
                        </div>
                    </div>

                    <div className="form-group">
                        <label>验证码</label>
                        <div className="captcha-group">
                            <input
                                type="text"
                                value={captchaCode}
                                onChange={(e) => setCaptchaCode(e.target.value)}
                                placeholder="请输入验证码"
                                required
                                maxLength={4}
                            />
                            <div className="captcha-img-wrapper" onClick={fetchCaptcha} title="点击刷新验证码">
                                {captchaImg ? (
                                    <img src={captchaImg} alt="验证码" className="captcha-img" />
                                ) : captchaError ? (
                                    <span className="captcha-placeholder">点击刷新</span>
                                ) : (
                                    <span className="captcha-placeholder">加载中...</span>
                                )}
                            </div>
                        </div>
                    </div>

                    <button type="submit" disabled={loading || !captchaId}>
                        {loading ? '登录中...' : '登录'}
                    </button>
                </form>

                <div className="login-hint">
                    默认账号: admin / admin123
                </div>
            </div>
        </div>
    );
}

export default Login;
