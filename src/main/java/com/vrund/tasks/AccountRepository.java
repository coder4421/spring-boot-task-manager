package com.vrund.tasks;
import org.springframework.data.jpa.repository.JpaRepository;
interface AccountRepository extends JpaRepository<Account, String> {}
