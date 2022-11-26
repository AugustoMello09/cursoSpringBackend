package com.jin.estudomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.estudomc.domain.Estado;
import com.jin.estudomc.repositories.EstadoRepository;

@Service
public class EstadoService {
	@Autowired
	private EstadoRepository repo;
	
	public List<Estado> findAll() {
		return repo.findAllByOrderByNome();
	}
}
