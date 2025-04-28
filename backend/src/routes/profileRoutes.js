const express = require('express');
const router = express.Router();
const profileController = require('../controllers/profileController');
const authMiddleware = require('../middleware/authMiddleware');

// Защищаем все маршруты профиля
router.use(authMiddleware);

// Получить цель по воде (GET /api/profile/water-goal)
router.get('/water-goal', profileController.getWaterGoal);

// Обновить цель по воде (PUT /api/profile/water-goal)
router.put('/water-goal', profileController.updateWaterGoal);

module.exports = router; 