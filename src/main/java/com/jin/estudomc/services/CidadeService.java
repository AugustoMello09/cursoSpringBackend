package com.jin.estudomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.estudomc.domain.Cidade;
import com.jin.estudomc.repositories.CidadeRepository;

@Service
public class CidadeService {
	@Autowired
	private CidadeRepository repo;
	
	public List<Cidade> findByEstado(Integer estadoId){
		return repo.findCidades(estadoId);
	}
}
