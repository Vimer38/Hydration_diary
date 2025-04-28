const express = require('express');
const router = express.Router();
const waterController = require('../controllers/waterController');
const authMiddleware = require('../middleware/authMiddleware'); // Импортируем middleware

// Применяем middleware ко всем маршрутам в этом файле
router.use(authMiddleware);

// Маршрут для добавления записи о воде (POST /api/water)
router.post('/', waterController.addRecord);

// Маршрут для получения всех записей о воде для пользователя (GET /api/water)
// Убрал, так как обычно нужны записи за день. Можно раскомментировать при необходимости.
// router.get('/', waterController.getAllRecords);

// Маршрут для получения записей о воде за сегодня (GET /api/water/today)
router.get('/today', waterController.getTodayRecords);

// Маршрут для получения суммарного потребления за сегодня (GET /api/water/today/total)
router.get('/today/total', waterController.getTodayTotal);

module.exports = router; 