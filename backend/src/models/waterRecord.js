const db = require('../config/database');

class WaterRecord {
  // Добавление записи о потреблении воды для конкретного пользователя
  static async create({ userId, amount }) {
    const query = `
      INSERT INTO water_records (user_id, amount)
      VALUES ($1, $2)
      RETURNING id, user_id, amount, record_time
    `;
    const values = [userId, amount];
    try {
      const result = await db.query(query, values);
      return result.rows[0];
    } catch (error) {
      console.error('Error creating water record:', error);
      throw error;
    }
  }

  // Получение всех записей о воде для конкретного пользователя
  static async findByUserId(userId) {
    const query = 'SELECT id, amount, record_time FROM water_records WHERE user_id = $1 ORDER BY record_time DESC';
    try {
      const result = await db.query(query, [userId]);
      return result.rows;
    } catch (error) {
      console.error('Error finding water records by user ID:', error);
      throw error;
    }
  }

  // Получение записей о воде для пользователя за определенную дату (например, за сегодня)
  static async findByUserIdAndDate(userId, date) {
    // date должен быть в формате 'YYYY-MM-DD'
    const query = `
      SELECT id, amount, record_time 
      FROM water_records 
      WHERE user_id = $1 AND DATE(record_time) = $2
      ORDER BY record_time DESC
    `;
    try {
      const result = await db.query(query, [userId, date]);
      return result.rows;
    } catch (error) {
      console.error('Error finding water records by user ID and date:', error);
      throw error;
    }
  }

  // Получение суммарного потребления воды пользователем за сегодня
   static async getTodayTotalByUserId(userId) {
    const query = `
      SELECT SUM(amount) as total
      FROM water_records 
      WHERE user_id = $1 AND DATE(record_time) = CURRENT_DATE
    `;
    try {
      const result = await db.query(query, [userId]);
      // Возвращаем total или 0, если записей нет (sum вернет null)
      return result.rows[0]?.total || 0; 
    } catch (error) {
      console.error('Error getting today\'s total water intake:', error);
      throw error;
    }
  }

  // Можно добавить методы для обновления и удаления записей, если нужно
}

module.exports = WaterRecord; 