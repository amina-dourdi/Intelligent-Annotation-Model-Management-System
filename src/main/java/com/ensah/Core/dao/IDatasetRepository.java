package com.ensah.Core.dao;

import com.ensah.Core.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDatasetRepository extends JpaRepository<Dataset, Long> {
        }