package com.ensah.Core.dao;


import com.ensah.Core.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUtilisateurRepository extends JpaRepository<Utilisateur, Long> {

boolean existsByLogin(String login);
}
