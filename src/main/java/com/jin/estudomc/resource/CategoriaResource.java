package com.jin.estudomc.resource;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.jin.estudomc.domain.Categoria;
import com.jin.estudomc.dto.CategoriaDTO;
import com.jin.estudomc.services.CategoriaService;

@RestController
@RequestMapping(value = "/categorias")
public class CategoriaResource {

	@Autowired
	private CategoriaService service;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Categoria> find(@PathVariable Integer id) {
		Categoria obj = service.find(id);
		return ResponseEntity.ok().body(obj);

	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid @RequestBody CategoriaDTO objDto) {
		Categoria obj = service.fromDTO(objDto);
		obj = service.insert(obj);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

		return ResponseEntity.created(uri).build();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody CategoriaDTO objDto, @PathVariable Integer id) {
		Categoria obj = service.fromDTO(objDto);
		obj.setId(id);
		obj = service.update(obj);
		return ResponseEntity.noContent().build();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);

		return ResponseEntity.noContent().build();

	}

	@RequestMapping( method = RequestMethod.GET)
	public ResponseEntity<List<CategoriaDTO>> findAll() {
	// busca as listas de categoria do Banco
	List<Categoria> list = service.findAll();
	// vai ter que percorrer a list e para cada elemento da lista vai instanciar o DTO correspondente(converter para DTO) 
	List<CategoriaDTO>	listDto = list.stream().map(obj -> new CategoriaDTO(obj)).collect(Collectors.toList());
	return ResponseEntity.ok().body(listDto);

	}

	@RequestMapping(value="/pages", method = RequestMethod.GET)
	public ResponseEntity<Page<CategoriaDTO>> findPage(
			@RequestParam(value ="page", defaultValue="0")Integer page, 
			@RequestParam(value ="linesPerPage", defaultValue="24")Integer linesPerPage,
			@RequestParam(value ="orderBy", defaultValue="nome")String orderBy,
			@RequestParam(value ="direction", defaultValue="ASC")String direction) {
	// busca as paginas de categoria do Banco
	Page<Categoria> list = service.findPage(page, linesPerPage, orderBy, direction);
	// vai ter que percorrer as páginas e para cada elemento da vai instanciar o DTO correspondente(converter para DTO) 
	Page<CategoriaDTO>	listDto = list.map(obj -> new CategoriaDTO(obj));
	return ResponseEntity.ok().body(listDto);

	}
}
