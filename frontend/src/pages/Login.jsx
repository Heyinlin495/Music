import { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../api';
import { FiEye } from 'react-icons/fi';
import './Auth.css';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [captchaCode, setCaptchaCode] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [captchaImg, setCaptchaImg] = useState('');
  const [captchaError, setCaptchaError] = useState(false);
  const { login, loading, error, clearError } = useAuthStore();
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
    } catch (err) {
      console.error('Failed to load captcha:', err);
      setCaptchaError(true);
    }
  }, []);

  useEffect(() => {
    fetchCaptcha();
  }, [fetchCaptcha]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!captchaId || !captchaCode) {
      return;
    }
    const success = await login(username, password, captchaId, captchaCode);
    if (success) {
      navigate('/');
    } else {
      fetchCaptcha();
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>欢迎回来Heyinlin!</h1>
        <p className="auth-subtitle">请登录</p>

        {error && (
          <div className="error-message" onClick={clearError}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">用户名</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="请输入用户名"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">密码</label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
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
                <FiEye />
              </button>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="captcha">验证码</label>
            <div className="captcha-group">
              <input
                type="text"
                id="captcha"
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

          <button type="submit" className="submit-btn" disabled={loading || !captchaId}>
            {loading ? '登录中...' : '登录'}
          </button>
        </form>

        <p className="auth-footer">
          还没有账户？ <Link to="/register">立即注册</Link>
        </p>

        <div className="demo-hint">
          <p>演示账户: demo / demo123</p>
        </div>
      </div>
    </div>
  );
}

export default Login;
