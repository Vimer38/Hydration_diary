const db = require('../config/database');
const bcrypt = require('bcryptjs');

class User {
  static async create({ username, email, password }) {
    const hashedPassword = await bcrypt.hash(password, 10);
    const query = `
      INSERT INTO users (username, email, password)
      VALUES ($1, $2, $3)
      RETURNING id, username, email, water_goal
    `;
    const values = [username, email, hashedPassword];
    const result = await db.query(query, values);
    return result.rows[0];
  }

  static async findByEmail(email) {
    const query = 'SELECT id, username, email, password, water_goal FROM users WHERE email = $1';
    const result = await db.query(query, [email]);
    return result.rows[0];
  }

  static async findById(id) {
    const query = 'SELECT id, username, email, water_goal FROM users WHERE id = $1';
    const result = await db.query(query, [id]);
    return result.rows[0];
  }

  static async updateWaterGoal(userId, waterGoal) {
    if (typeof waterGoal !== 'number' || waterGoal <= 0) {
      throw new Error('Invalid water goal value');
    }
    const query = `
      UPDATE users
      SET water_goal = $1
      WHERE id = $2
      RETURNING id, username, email, water_goal
    `;
    const values = [waterGoal, userId];
    try {
      const result = await db.query(query, values);
      if (result.rows.length === 0) {
        throw new Error('User not found');
      }
      return result.rows[0];
    } catch (error) {
      console.error('Error updating water goal:', error);
      throw error;
    }
  }
}

module.exports = User; 