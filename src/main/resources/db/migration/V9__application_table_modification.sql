alter table job_applications
add column status ENUM('APPLIED', 'ACCEPTED','REJECTED') DEFAULT 'APPLIED';
