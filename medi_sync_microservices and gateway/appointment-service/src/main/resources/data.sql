-- Pre-populated appointment slots for demo
-- These are inserted when the service starts
-- Covers the main departments AI triage can suggest

INSERT INTO appointment_slots (id, doctor_name, department, slot_time, status)
VALUES
-- Cardiology
('a1000000-0000-0000-0000-000000000001', 'Dr. Mehta',  'Cardiology',       '2025-09-01 09:00:00', 'AVAILABLE'),
('a1000000-0000-0000-0000-000000000002', 'Dr. Mehta',  'Cardiology',       '2025-09-01 11:00:00', 'AVAILABLE'),
('a1000000-0000-0000-0000-000000000003', 'Dr. Sharma', 'Cardiology',       '2025-09-02 10:00:00', 'AVAILABLE'),

-- Orthopedics
('a2000000-0000-0000-0000-000000000001', 'Dr. Patel',  'Orthopedics',      '2025-09-01 09:30:00', 'AVAILABLE'),
('a2000000-0000-0000-0000-000000000002', 'Dr. Patel',  'Orthopedics',      '2025-09-01 14:00:00', 'AVAILABLE'),
('a2000000-0000-0000-0000-000000000003', 'Dr. Joshi',  'Orthopedics',      '2025-09-03 11:00:00', 'AVAILABLE'),

-- Neurology
('a3000000-0000-0000-0000-000000000001', 'Dr. Gupta',  'Neurology',        '2025-09-01 10:00:00', 'AVAILABLE'),
('a3000000-0000-0000-0000-000000000002', 'Dr. Gupta',  'Neurology',        '2025-09-02 09:00:00', 'AVAILABLE'),

-- General Medicine
('a4000000-0000-0000-0000-000000000001', 'Dr. Khan',   'General Medicine', '2025-09-01 08:30:00', 'AVAILABLE'),
('a4000000-0000-0000-0000-000000000002', 'Dr. Khan',   'General Medicine', '2025-09-01 12:00:00', 'AVAILABLE'),
('a4000000-0000-0000-0000-000000000003', 'Dr. Verma',  'General Medicine', '2025-09-02 15:00:00', 'AVAILABLE'),

-- ENT
('a5000000-0000-0000-0000-000000000001', 'Dr. Singh',  'ENT',              '2025-09-01 13:00:00', 'AVAILABLE'),
('a5000000-0000-0000-0000-000000000002', 'Dr. Singh',  'ENT',              '2025-09-03 09:00:00', 'AVAILABLE'),

-- Gastroenterology
('a6000000-0000-0000-0000-000000000001', 'Dr. Desai',  'Gastroenterology', '2025-09-02 11:00:00', 'AVAILABLE'),
('a6000000-0000-0000-0000-000000000002', 'Dr. Desai',  'Gastroenterology', '2025-09-03 14:00:00', 'AVAILABLE'),

-- Dermatology
('a7000000-0000-0000-0000-000000000001', 'Dr. Rao',    'Dermatology',      '2025-09-01 15:00:00', 'AVAILABLE'),
('a7000000-0000-0000-0000-000000000002', 'Dr. Rao',    'Dermatology',      '2025-09-02 16:00:00', 'AVAILABLE')

    ON CONFLICT (id) DO NOTHING;
