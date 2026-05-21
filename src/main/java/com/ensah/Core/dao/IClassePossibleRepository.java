package com.ensah.Core.dao;



import com.ensah.Core.model.ClassePossible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IClassePossibleRepository extends JpaRepository<ClassePossible, Long> {
        }