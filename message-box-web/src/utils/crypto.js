import CryptoJS from 'crypto-js';

// 密钥和IV，必须与后端保持一致
// 注意：在生产环境中，密钥管理需要更安全的方式，例如通过安全渠道从后端获取或使用非对称加密。
const KEY = CryptoJS.enc.Utf8.parse('1234567890123456'); // 16位密钥
const IV = CryptoJS.enc.Utf8.parse('1234567890123456'); // 16位IV

/**
 * AES 加密
 * @param {string} text - 要加密的文本
 * @returns {string} - 加密后的Base64编码字符串
 */
export function encrypt(text) {
  const encrypted = CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(text), KEY, {
    iv: IV,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
  });
  return encrypted.toString(); // 返回Base64编码的密文
}

/**
 * AES 解密
 * @param {string} encryptedText - Base64编码的加密字符串
 * @returns {string} - 解密后的文本
 */
export function decrypt(encryptedText) {
  const decrypted = CryptoJS.AES.decrypt(encryptedText, KEY, {
    iv: IV,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
  });
  return decrypted.toString(CryptoJS.enc.Utf8);
} 