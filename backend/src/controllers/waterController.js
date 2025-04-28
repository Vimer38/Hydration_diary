const WaterRecord = require('../models/waterRecord');

const waterController = {
  // Добавление записи о воде
  async addRecord(req, res) {
    try {
      const { amount } = req.body; // Получаем количество из тела запроса
      const userId = req.userId; // Получаем ID пользователя из middleware

      if (!amount || typeof amount !== 'number' || amount <= 0) {
        return res.status(400).json({ message: 'Invalid amount specified' });
      }

      const newRecord = await WaterRecord.create({ userId, amount });
      res.status(201).json(newRecord);
    } catch (error) {
      res.status(500).json({ message: 'Error adding water record', error: error.message });
    }
  },

  // Получение всех записей о воде для текущего пользователя
  async getAllRecords(req, res) {
    try {
      const userId = req.userId; // Получаем ID пользователя из middleware
      const records = await WaterRecord.findByUserId(userId);
      res.json(records);
    } catch (error) {
      res.status(500).json({ message: 'Error fetching water records', error: error.message });
    }
  },

  // Получение записей о воде для текущего пользователя за сегодня
  async getTodayRecords(req, res) {
      try {
        const userId = req.userId;
        const today = new Date().toISOString().split('T')[0]; // Получаем сегодняшнюю дату в формате 'YYYY-MM-DD'
        const records = await WaterRecord.findByUserIdAndDate(userId, today);
        res.json(records);
      } catch (error) {
          res.status(500).json({ message: 'Error fetching today\'s water records', error: error.message });
      }
  },

  // Получение суммарного потребления воды за сегодня
  async getTodayTotal(req, res) {
      try {
        const userId = req.userId;
        const total = await WaterRecord.getTodayTotalByUserId(userId);
        res.json({ total }); // Возвращаем объект { total: <значение> }
      } catch (error) {
          res.status(500).json({ message: 'Error fetching today\'s total water intake', error: error.message });
      }
  }
};

module.exports = waterController; 