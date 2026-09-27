const express = require("express");
const cors = require("cors");
const { Pool } = require("pg");

const app = express();

app.use(cors());
app.use(express.json());

const pool = new Pool({
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT || 5432),
  database: process.env.DB_NAME || "employee_db",
  user: process.env.DB_USER || "employee_user",
  password: process.env.DB_PASSWORD || "employee_password"
});

app.get("/api/health", async (req, res) => {
  try {
    await pool.query("SELECT 1");
    res.json({ status: "UP", database: "CONNECTED" });
  } catch (error) {
    res.status(500).json({ status: "DOWN", error: error.message });
  }
});

app.get("/api/employees", async (req, res) => {
  try {
    const result = await pool.query(
      "SELECT id, name, email, department FROM employees ORDER BY id"
    );
    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/employees", async (req, res) => {
  const { name, email, department } = req.body;

  if (!name || !email || !department) {
    return res.status(400).json({
      error: "name, email and department are required"
    });
  }

  try {
    const result = await pool.query(
      `INSERT INTO employees (name, email, department)
       VALUES ($1, $2, $3)
       RETURNING id, name, email, department`,
      [name, email, department]
    );

    res.status(201).json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

const PORT = Number(process.env.PORT || 5006);

app.listen(PORT, () => {
  console.log(`Backend running on port ${PORT}`);
});
