const User = require('../models/user');

const profileController = {
    // Получение текущей цели по воде
    async getWaterGoal(req, res) {
        try {
            const userId = req.userId; // Получаем ID из authMiddleware
            const user = await User.findById(userId);
            if (!user) {
                return res.status(404).json({ message: 'User not found' });
            }
            res.json({ waterGoal: user.water_goal });
        } catch (error) {
            res.status(500).json({ message: 'Error fetching water goal', error: error.message });
        }
    },

    // Обновление цели по воде
    async updateWaterGoal(req, res) {
        try {
            const userId = req.userId;
            const { waterGoal } = req.body; // Ожидаем { "waterGoal": новое_значение }

            if (waterGoal === undefined || typeof waterGoal !== 'number' || waterGoal <= 0) {
                return res.status(400).json({ message: 'Invalid waterGoal value provided' });
            }

            // Вызываем метод модели для обновления
            const updatedUser = await User.updateWaterGoal(userId, waterGoal);
            res.json({ message: 'Water goal updated successfully', waterGoal: updatedUser.water_goal });

        } catch (error) {
            if (error.message === 'User not found') {
                 return res.status(404).json({ message: 'User not found' });
            }
             if (error.message === 'Invalid water goal value') {
                 return res.status(400).json({ message: error.message });
            }
            res.status(500).json({ message: 'Error updating water goal', error: error.message });
        }
    }
};

module.exports = profileController; 