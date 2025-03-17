INSERT INTO orders (description, status) VALUES
('New order placed', 'PENDING'),
('Order confirmed', 'PROCESSING'),
('Shipped to customer', 'COMPLETED'),
('Customer requested cancellation', 'CANCELLED'),
('Awaiting payment', 'PENDING'),
('Processing refund', 'PROCESSING'),
('Delivered successfully', 'COMPLETED');

INSERT INTO users (username, role, password) VALUES
('John', 'ADMIN', '$2a$10$Y5kU5FkEoB4A9jD7ZnVfXOa9kMkvSBDpSvsIQOVgixlxhj0NO7Y5S'),
('Jane', 'USER', '$2a$10$TdFZpKQHFednGQ4IcI5ckls4Zf8D9RfT38MvT8BD8GpK5p2Ebi3j7K'),
('Emily', 'USER', '$2a$10$h0Vo4hbwDz5SuZnqE.IgrFb3uW9YZlC9DZdYXqxX8P/jNHL92jSFi'),
('Michael', 'USER', '$2a$10$BsI5llWXL1QddzXJlWw6lD6VSbnNCp1f.wgnXY1VQJktUUR0BZyRi'),
('Sarah', 'ADMIN', '$2a$10$JG9f5YabkmfZT.f4mnz/Xz0Iu3fSBPLT2qZqZTqaNr7g91hml09Eq');