package com.ensah.Core.dao;

import com.ensah.Core.model.Annotateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAnnotateurRepository extends JpaRepository<Annotateur, Long> {

    Optional<Annotateur> findByLogin(String login);
List<Annotateur> findByActifTrue();
Optional<Annotateur> findByFirstLoginToken(String token);}