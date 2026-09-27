CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    department VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO employees (name, email, department)
VALUES
    ('John Doe', 'john@example.com', 'QA'),
    ('Jane Smith', 'jane@example.com', 'Engineering')
ON CONFLICT (email) DO NOTHING;
