import axios from 'axios';

const BASE_URL = 'http://localhost:8081/api';

/**
 * Gửi 1 message từ 1 user
 */
export const sendMessage = (userId, content) =>
  axios.post(`${BASE_URL}/send`, { userId, content });

/**
 * Giả lập nhiều user gửi đồng thời
 */
export const triggerSpam = (users = 10, messagesPerUser = 100) =>
  axios.post(`${BASE_URL}/spam`, { users, messagesPerUser });

/**
 * Lấy thông tin cấu hình topic
 */
export const getTopicInfo = () =>
  axios.get(`${BASE_URL}/topic-info`);

