const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');
require('dotenv').config();

const authRoutes = require('./routes/authRoutes');
const waterRoutes = require('./routes/waterRoutes');
const profileRoutes = require('./routes/profileRoutes');

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Database connection
const pool = new Pool({
  user: process.env.DB_USER,
  host: process.env.DB_HOST,
  database: process.env.DB_NAME,
  password: process.env.DB_PASSWORD,
  port: process.env.DB_PORT,
});

// Test database connection
pool.query('SELECT NOW()', (err, res) => {
  if (err) {
    console.error('Error connecting to the database:', err);
  } else {
    console.log('Database connected successfully');
  }
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/water', waterRoutes);
app.use('/api/profile', profileRoutes);

app.get('/', (req, res) => {
  res.json({ message: 'Welcome to Healthy Nutrition API' });
});

// Start server
app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
}); 