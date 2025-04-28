const jwt = require('jsonwebtoken');

const authMiddleware = (req, res, next) => {
  // Получаем токен из заголовка Authorization (формат: Bearer <token>)
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'Authentication token required' });
  }

  const token = authHeader.split(' ')[1];

  try {
    // Верифицируем токен
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // Добавляем userId в объект запроса для использования в контроллерах
    req.userId = decoded.userId;
    next(); // Переходим к следующему middleware или контроллеру
  } catch (error) {
    console.error('Token verification error:', error);
    if (error.name === 'TokenExpiredError') {
        return res.status(401).json({ message: 'Token expired' });
    }
    return res.status(401).json({ message: 'Invalid token' });
  }
};

module.exports = authMiddleware; 