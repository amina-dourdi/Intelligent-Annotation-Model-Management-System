package com.ensah.Core.dao;

import com.ensah.Core.model.Annotateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAnnotateurRepository extends JpaRepository<Annotateur, Long> {

List<Annotateur> findByActifTrue();
}