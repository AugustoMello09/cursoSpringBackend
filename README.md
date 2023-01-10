# Backend

## CRUD

### Objetivo geral:

Implementar operações de CRUD e de casos de uso conforme boas práticas de Engenharia de Software.
Competências:
- Implementar requisições POST, GET, PUT e DELETE para inserir, obter, atualizar e deletar
entidades, respectivamente, seguindo boas práticas REST e de desenvolvimento em camadas.
- Trabalhar com DTO (Data Transfer Object)
- Trabalhar com paginação de dados
- Trabalhar com validação de dados com Bean Validation (javax.validation)
- Criar validações customizadas
- Fazer tratamento adequado de exceções (incluindo integridade referencial e validação)
- Efetuar consultas personalizadas ao banco de dados
Nota: ressaltamos novamente que CRUD's também são casos de uso, mas estamos chamando de casos de uso os
usos do sistema correspondentes a processos de negócio que não se enquadram em CRUD's comuns

### Anotações de estudo

- Trabalhar com DTO (Data Transfer Object),Ele vai ser o objeto para definir os dados que você quer trafegar quando for fazer operações básicas

## Apresentando o caso de uso

![registrar pedido](https://user-images.githubusercontent.com/101072311/201928965-44a519fd-8cf1-4a1a-93c6-0dbc6fccd283.png)

![cenários adversos](https://user-images.githubusercontent.com/101072311/201928939-1f2606af-d7f7-4922-823e-21168aa3af0b.png)


#### Criando o método POST

(Resource)
~~~JAVA
@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> insert(@RequestBody Categoria obj) {
		obj = service.insert(obj);
    // chamada que pega a URI do novo recurso que foi inserido
    URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

		return ResponseEntity.created(uri).build();
	}
~~~
(Service)
~~~JAVA
public Categoria insert(Categoria obj) {
		obj.setId(null);
		return repo.save(obj);
	}
~~~

Testando no Postmam (categorias/{id} POST )

![POST1](https://user-images.githubusercontent.com/101072311/200845019-62876c92-eedb-4332-b0a2-308d0479db14.png)

![POST2](https://user-images.githubusercontent.com/101072311/200845032-25f4b4e8-2a56-4154-88f7-e032e92cb95a.png)

#### Criando o método PUT
(Resource)
~~~JAVA
@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@RequestBody Categoria obj, @PathVariable Integer id){
		obj.setId(id);
		obj = service.update(obj);
		return ResponseEntity.noContent().build();
	}
~~~
(Service)
~~~JAVA
public Categoria update(Categoria obj) {
		find(obj.getId());
		return repo.save(obj);
	}
~~~

#### Criando o método DELETE
(Resource)
~~~JAVA
@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);

		return ResponseEntity.noContent().build();
	}
~~~
##### Tratamento de Erro e mensagem personalizada

~~~JAVA
public class DataIntegrityException  extends RuntimeException{


	private static final long serialVersionUID = 1L;

	public DataIntegrityException(String msg) {
		super(msg);
	}

	public DataIntegrityException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
~~~

~~~JAVA
@ExceptionHandler(DataIntegrityException.class)
	public ResponseEntity<StandardError> dataIntegrity(DataIntegrityException e, HttpServletRequest request){

		StandardError err = new StandardError(HttpStatus.BAD_REQUEST.value(), e.getMessage(), System.currentTimeMillis());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}
~~~

![deleteerro](https://user-images.githubusercontent.com/101072311/200844990-570c1830-b46e-4764-985e-b7fa2e24d807.png)

(Service)
~~~JAVA
public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		}
		 catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir uma categoria que possui produto");
		}
	}
~~~

Testando no Postmam (categorias/{id} DELETE )

![POST2](https://user-images.githubusercontent.com/101072311/200845032-25f4b4e8-2a56-4154-88f7-e032e92cb95a.png)

![deletado](https://user-images.githubusercontent.com/101072311/200844975-78a60321-2e24-4b10-ba71-86591062f151.png)

#### Criando a CategoriaDTO
Ele vai ser o objeto para definir os dados que você quer trafegar quando for fazer operações básicas de Categoria.
Obs:Nela também acompanha  Getters e Setters, mas não foi colocado na parte da documentação para não atrapalhar a visualização.

(DTO)
~~~JAVA
public class CategoriaDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String nome;

	public CategoriaDTO() {
	}

  public CategoriaDTO(Categoria obj) {
		id = obj.getId();
		nome = obj.getNome();
	}
}  
~~~

#### método GET que lista todas as categorias

(Resource)
~~~JAVA
@RequestMapping( method = RequestMethod.GET)
	public ResponseEntity<List<CategoriaDTO>> findAll() {
	// busca as listas de categoria do Banco
	List<Categoria> list = service.findAll();
	// vai ter que percorrer a list e para cada elemento da lista vai instanciar o DTO correspondente(converter para DTO)
	List<CategoriaDTO>	listDto = list.stream().map(obj -> new CategoriaDTO(obj)).collect(Collectors.toList());
	return ResponseEntity.ok().body(listDto);
	}
~~~

Testando no Postman categorias/

![getcategorias](https://user-images.githubusercontent.com/101072311/200922718-01292407-7fbd-47bc-b98d-e68985b268a7.png)

#### paginação com parâmetros opcionais na requisição

(Resouce)
~~~JAVA
@RequestMapping(value="/pages", method = RequestMethod.GET)
	public ResponseEntity<Page<CategoriaDTO>> findPage(
			@RequestParam(value ="page", defaultValue="0")Integer page,
			@RequestParam(value ="linesPerPage", defaultValue="24")Integer linesPerPage,
			@RequestParam(value ="orderBy", defaultValue="nome")String orderBy,
			@RequestParam(value ="direction", defaultValue="ASC")String direction) {
	// busca as páginas de categoria do Banco
	Page<Categoria> list = service.findPage(page, linesPerPage, orderBy, direction);
	// vai ter que percorrer as páginas e para cada elemento da vai instanciar o DTO correspondente(converter para DTO)
	Page<CategoriaDTO>	listDto = list.map(obj -> new CategoriaDTO(obj));
	return ResponseEntity.ok().body(listDto);

	}
~~~
(Service)
~~~JAVA
public Page<Categoria> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction),
				orderBy);
		return repo.findAll(pageRequest);
	}
~~~

testando no Postmam (categorias/pages?linesPerPage=3)

![linesPerPagestest](https://user-images.githubusercontent.com/101072311/201099282-b5049464-dade-4680-a49d-aa16a74ceaf0.png)

### Validações de dados

- __Sem acesso a dados__
	* __Sintáticas__
		- Campo não pode ser vazio
		- Valor numérico mínimo  e máximo
		- Comprimento de string mínimo e máximo
		- Somente dígitos
		- Padrão (expressão regular): (##)-####-####
	* __Outros__
		- Data futura / passada

	* __Mais de um campo__
		-	 Confirmação de senha igual à senha

- __Com acesso a dados__			
	* Email não pode ser repedito
	* Cada cliente pode cadastrar no máximo três cupons por mês


#### validação sintática bom Bean Validation
(CategoriaDTO)
~~~JAVA
@NotEmpty(message="Preenchimento obrigatório")
	@Length(min=5, max=80, message="O tamanho deve ser entre 5 e 80 caracteres")
	private String nome;
~~~
(CategoriaService)
~~~JAVA
// método auxiliar que instancia uma categoria através de um DTO
	public Categoria fromDTO(CategoriaDTO objDto) {
		return new Categoria(objDto.getId(), objDto.getNome());
	}
~~~

(CategoriaResource) Atualização no POST
~~~JAVA
@RequestMapping(method = RequestMethod.POST)

	public ResponseEntity<Void> insert(@Valid @RequestBody CategoriaDTO objDto) {
		Categoria obj = service.fromDTO(objDto);
		obj = service.insert(obj);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

		return ResponseEntity.created(uri).build();
	}
}
~~~
(CategoriaResource) Atualização no PUT
~~~JAVA
@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody CategoriaDTO objDto, @PathVariable Integer id) {
		Categoria obj = service.fromDTO(objDto);
		obj.setId(id);
		obj = service.update(obj);
		return ResponseEntity.noContent().build();
}		
~~~
Testando o Preenchimento obrigatório

![vazio](https://user-images.githubusercontent.com/101072311/201107061-e7c8ade5-d0c9-433b-884b-33f3784516c5.png)

![Putcom erro](https://user-images.githubusercontent.com/101072311/201099228-2e3e0630-55eb-4203-bd49-7448390f459b.png)

Testando o limite de criação

![até4](https://user-images.githubusercontent.com/101072311/201107051-02718fde-a940-47f7-b869-d3c67998a0a7.png)


![putcomerro](https://user-images.githubusercontent.com/101072311/201099243-6e1c9ee1-0eb3-450d-b574-73f64692e0f5.png)

##### Personalizando a validação

(Criando a FielMessage)
~~~JAVA
public class FieldMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private String fieldName;
	private String message;

	public FieldMessage() {

	}

	public FieldMessage(String fieldName, String message) {
		super();
		this.fieldName = fieldName;
		this.message = message;
	}

	public String getFielName() {
		return fieldName;
	}

	public void setFielName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
~~~
(Criando validationError)
~~~JAVA
public class ValidationError extends StandardError {

	private static final long serialVersionUID = 1L;


	private List<FieldMessage> errors  = new ArrayList<>();


	public ValidationError(Integer status, String msg, Long timeStamp) {
		super(status, msg, timeStamp);

	}


	public List<FieldMessage> getErrors() {
		return errors;
	}


	public void addError(String fieldName, String messagem) {
		errors.add(new FieldMessage(fieldName, messagem));
	}

}
~~~

#### Criando a ClienteDTO
Ele vai ser o objeto para definir os dados que você quer trafegar quando for fazer operações básicas de cliente e com algumas validações.
Obs:Nela também acompanha  Getters e Setters, mas não foi colocado na parte da documentação para não atrapalhar a visualização.

(DTO)
~~~JAVA
public class ClienteDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;

	@NotEmpty(message="Preenchimento obrigatório")
	@Length(min=5, max=120, message="O tamanho deve ser entre 5 e 120 caracteres")
	private String nome;

	@NotEmpty(message="Preenchimento obrigatório")
	@Email(message="Email inválido")
	private String email;
}	  
~~~

#### método GET/PUT e DELETE para cliente

(Resource)
~~~JAVA
@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody ClienteDTO objDto, @PathVariable Integer id) {
		Cliente obj = service.fromDTO(objDto);
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
	public ResponseEntity<List<ClienteDTO>> findAll() {
	// busca as listas de clientes do Banco
	List<Cliente> list = service.findAll();
	// vai ter que percorrer a list e para cada elemento da lista vai instanciar o DTO correspondente(converter para DTO)
	List<ClienteDTO>	listDto = list.stream().map(obj -> new ClienteDTO(obj)).collect(Collectors.toList());
	return ResponseEntity.ok().body(listDto);

	}
~~~

#### paginação com parâmetros opcionais na requisição

(Resouce)
~~~JAVA
@RequestMapping(value="/pages", method = RequestMethod.GET)
	public ResponseEntity<Page<ClienteDTO>> findPage(
			@RequestParam(value ="page", defaultValue="0")Integer page,
			@RequestParam(value ="linesPerPage", defaultValue="24")Integer linesPerPage,
			@RequestParam(value ="orderBy", defaultValue="nome")String orderBy,
			@RequestParam(value ="direction", defaultValue="ASC")String direction) {
	// busca as paginas de clientes do Banco
	Page<Cliente> list = service.findPage(page, linesPerPage, orderBy, direction);
	// vai ter que percorrer as páginas e para cada elemento da vai instanciar o DTO correspondente(converter para DTO)
	Page<ClienteDTO>	listDto = list.map(obj -> new ClienteDTO(obj));
	return ResponseEntity.ok().body(listDto);

	}
~~~
(Service)
~~~JAVA
public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);

		return repo.save(newObj);
	}

	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		}
		 catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há entidades relacionadas");
		}
	}

	public List<Cliente> findAll() {
		return repo.findAll();
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction),
				orderBy);
		return repo.findAll(pageRequest);
	}

	// método auxiliar que instancia uma cliente através de um DTO
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null);
	}

	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
~~~

#### Corrigindo erro Null no domain cliente e pagamento

~~~JAVA
this.tipo = (tipo==null) ? null:tipo.getCod();
~~~

~~~JAVA
this.estado = (estado==null) ? null:estado.getCod();
~~~

#### Inserindo um novo cliente com POST

##### Criando um DTO especifico para enviar todos os dados
Para um cliente ter acesso o banco de dales ele precisa de um endereço e um telefone
Obs:Nela também acompanha  Getters e Setters, mas não foi colocado na parte da documentação para não atrapalhar a visualização.

(ClienteNewDTO)
~~~JAVA
public class ClienteNewDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String nome;
	private String email;
	private String cpfOuCnpj;
	private Integer tipo;

	private String logradouro;
	private String numero;
	private String complemento;
	private String bairro;
	private String cep;

	private String telefone1;
	private String telefone2;
	private String telefone3;

	private Integer cidadeId;

	public ClienteNewDTO() {
	}
}
~~~

##### Criando o Endpoint de POST no ClienteResource/ClienteService

(ClienteService)
~~~JAVA
@Autowired
	private EnderecoRepository enderecoRepository;

@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
}

public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(), TipoCliente.toEnum(objDto.getTipo()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2()!=null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3()!=null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}
~~~

(ClienteResource)
~~~JAVA
@RequestMapping(method=RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid @RequestBody ClienteNewDTO objDto) {
		Cliente obj = service.fromDTO(objDto);
		obj = service.insert(obj);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(obj.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

~~~

#### Validação customizada: CPF ou CNPJ na inserção de Cliente
Fazendo a validação com as Anotações

(ClienteNewDTO)
~~~JAVA
@ClienteInsert
public class ClienteNewDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotEmpty(message="Preenchimento obrigatório")
	@Length(min=5, max=120, message="O tamanho deve ser entre 5 e 120 caracteres")
	private String nome;

	@NotEmpty(message="Preenchimento obrigatório")
	@Email(message="Email inválido")
	private String email;

	@NotEmpty(message="Preenchimento obrigatório")
	private String cpfOuCnpj;

	private Integer tipo;

	@NotEmpty(message="Preenchimento obrigatório")
	private String logradouro;

	@NotEmpty(message="Preenchimento obrigatório")
	private String numero;

	private String complemento;

	private String bairro;

	@NotEmpty(message="Preenchimento obrigatório")
	private String cep;

	@NotEmpty(message="Preenchimento obrigatório")
	private String telefone1;
}
~~~

##### Criando a anotação ClienteInsert

(ClienteInsert)
~~~JAVA
@Constraint(validatedBy = ClienteInsertValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClienteInsert {
	String message() default "Erro de validação";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
~~~

##### Criando o ClienteInsertValidator

(ClienteInsertValidatornsert)
~~~JAVA
public class ClienteInsertValidator implements ConstraintValidator<ClienteInsert, ClienteNewDTO> {
	@Override
	public void initialize(ClienteInsert ann) {
	}

	@Override
	public boolean isValid(ClienteNewDTO objDto, ConstraintValidatorContext context) {

		List<FieldMessage> list = new ArrayList<>();

		if(objDto.getTipo().equals(TipoCliente.PESSOAFISICA.getCod()) && !BR.isValidCPF(objDto.getCpfOuCnpj())) {
			list.add(new FieldMessage("cpfOuCnpj", "CPF inválido"));
		}

		if(objDto.getTipo().equals(TipoCliente.PESSOAJURIDICA.getCod()) && !BR.isValidCNPJ(objDto.getCpfOuCnpj())) {
			list.add(new FieldMessage("cpfOuCnpj", "CNPJ inválido"));
		}

		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
}
~~~

##### Validação de CPF e CNPJ

~~~JAVA
public class BR {
	private static final int[] weightSsn = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

    // CNPJ
    private static final int[] weightTin = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private static int calculate(final String str, final int[] weight) {
        int sum = 0;
        for (int i = str.length() - 1, digit; i >= 0; i--) {
            digit = Integer.parseInt(str.substring(i, i + 1));
            sum += digit * weight[weight.length - str.length() + i];
        }
        sum = 11 - sum % 11;
        return sum > 9 ? 0 : sum;
    }

    /**
     * Valida CPF
     *
     * @param ssn
     * @return
     */
    public static boolean isValidCPF(final String ssn) {
        if ((ssn == null) || (ssn.length() != 11) || ssn.matches(ssn.charAt(0) + "{11}")) return false;

        final Integer digit1 = calculate(ssn.substring(0, 9), weightSsn);
        final Integer digit2 = calculate(ssn.substring(0, 9) + digit1, weightSsn);
        return ssn.equals(ssn.substring(0, 9) + digit1.toString() + digit2.toString());
    }

    /**
     * Valida CNPJ
     *
     * @param tin
     * @return
     */
    public static boolean isValidCNPJ(final String tin) {
        if ((tin == null) || (tin.length() != 14) || tin.matches(tin.charAt(0) + "{14}")) return false;

        final Integer digit1 = calculate(tin.substring(0, 12), weightTin);
        final Integer digit2 = calculate(tin.substring(0, 12) + digit1, weightTin);
        return tin.equals(tin.substring(0, 12) + digit1.toString() + digit2.toString());
    }

}
~~~

#### Validação customizada:  Email não repetido na inserção Cliente

(Cliente)
~~~JAVA
@Column(unique=true)
	private String email;
~~~

(ClienteRepository)
~~~JAVA
@Transactional(readOnly=true)
	Cliente findByEmail(String email);
~~~

(ClienteInsertValidator)
~~~JAVA
@Autowired
	private ClienteRepository repo;

	Cliente aux = repo.findByEmail(objDto.getEmail());
		if (aux != null) {
			list.add(new FieldMessage("email", "Email já existe"));
		}
~~~

#### Busca de produtos por nome e categorias

(ProdutoDTO)
~~~JAVA
public class ProdutoDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;

	private String nome;
	private Double preco;

	public ProdutoDTO() {

	}
}
~~~

(ProdutoRepository)
~~~JAVA
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {


	Page<Produto> findDistinctByNomeContainingAndCategoriasIn( String nome, List<Categoria> categorias, Pageable pageRequest);
}
~~~

(utils)
~~~JAVA
public class URL {

	public static String decodeParam(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {

			return "";
		}
	}

	public static List<Integer> decodeIntList(String s) {
		String[] vet = s.split(",");
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < vet.length; i++) {
			list.add(Integer.parseInt(vet[i]));
		}
		return list;
	}
}
~~~

(ProdutoResource)
~~~JAVA
@RestController
@RequestMapping(value = "/produtos")
public class ProdutoResource {

	@Autowired
	private ProdutoService service;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Produto> find(@PathVariable Integer id) {
		Produto obj = service.find(id);
		return ResponseEntity.ok().body(obj);

	}

	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<Page<ProdutoDTO>> findPage(
			@RequestParam(value="nome", defaultValue="") String nome,
			@RequestParam(value="categorias", defaultValue="") String categorias,
			@RequestParam(value="page", defaultValue="0") Integer page,
			@RequestParam(value="linesPerPage", defaultValue="24") Integer linesPerPage,
			@RequestParam(value="orderBy", defaultValue="nome") String orderBy,
			@RequestParam(value="direction", defaultValue="ASC") String direction) {
		String nomeDecoded = URL.decodeParam(nome);
		List<Integer> ids = URL.decodeIntList(categorias);
		Page<Produto> list = service.search(nomeDecoded, ids, page, linesPerPage, orderBy, direction);
		Page<ProdutoDTO> listDto = list.map(obj -> new ProdutoDTO(obj));  
		return ResponseEntity.ok().body(listDto);
	}

}
~~~

(ProdutoService)
~~~JAVA
@Service
public class ProdutoService {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ProdutoRepository repo;

	public Produto find(Integer id) {
		 Optional<Produto> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
		 "Objeto não encontrado! Id: " + id + ", Tipo: " + Produto.class.getName()));
		}

	public Page<Produto> search(String nome, List<Integer> ids,Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		List<Categoria> categorias = categoriaRepository.findAllById(ids);
		return repo.findDistinctByNomeContainingAndCategoriasIn(nome, categorias, pageRequest);

	}
}
~~~

## Serviço de email

#### Objetivo geral:
 __Criar um serviço de email__
- Criar uma operação de envio de confirmação de pedido

 __Implementar o serviço em modo de desenvolvimento e produção__
- Criar o MockEmailService com Logger
- Criar o SmtpEmailService com SMTP do Google
__Demonstrar uma implementação flexível e elegante com padrões de projeto (Strategy e Template
Method)__

### Craindo um serviço de email
(TestConfig)
~~~JAVA
@Bean
	public EmailService emailService() {
		return new MockEmailService();
	}
~~~
(AbstractEmailService)
~~~JAVA
public abstract class  AbstractEmailService implements EmailService {


	@Value("${default.sender}")
	private String sender;

	@Override
	public void sendOrderConfirmationEmail(Pedido obj) {
		SimpleMailMessage sm = prepareSimpleMailMessageFromPedido(obj);
		sendEmail(sm);
	}

	protected SimpleMailMessage prepareSimpleMailMessageFromPedido(Pedido obj) {
		SimpleMailMessage sm = new SimpleMailMessage();
		sm.setTo(obj.getCliente().getEmail());
		sm.setFrom(sender);
		sm.setSubject("Pedido confirmado! Código: " + obj.getId());
		sm.setSentDate(new Date(System.currentTimeMillis()));
		sm.setText(obj.toString());
		return sm;
	}
}
~~~
(EmailService)
~~~JAVA
public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);

	void sendEmail(SimpleMailMessage msg);
}
~~~
(MockEmailService)
~~~JAVA
public class MockEmailService extends AbstractEmailService {

	private static final Logger LOG = LoggerFactory.getLogger(MockEmailService.class);

	@Override
	public void sendEmail(SimpleMailMessage msg) {
		LOG.info("Simulando envio de email...");
		LOG.info(msg.toString());
		LOG.info("Email enviado");
	}

}
~~~

(PedidoService) // alteração do pedidoService no insert
~~~JAVA
@Autowired
	private EmailService emailService;

	@Transactional
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		// alteração do pedidoService
		emailService.sendOrderConfirmationEmail(obj);
		return obj;
	}
}
~~~

### Implementando SmtpEmailService com servidor google

(DevConfig)
~~~JAVA
@Bean
	public EmailService emailService() {
		return new SmtpEmailService();
	}
~~~
(SmtpEmailService)
~~~JAVA
public class SmtpEmailService extends AbstractEmailService {

	@Autowired
	private MailSender mailSender;

	private static final Logger LOG = LoggerFactory.getLogger(SmtpEmailService.class);

	@Override
	public void sendEmail(SimpleMailMessage msg) {
		LOG.info("Simulando envio de email...");
		mailSender.send(msg);
		LOG.info("Email enviado");

	}

}
~~~

### envio de email com html

(AbstractEmailService)
~~~JAVA
@Autowired
	private JavaMailSender javaMailSender;


	@Autowired
	private TemplateEngine templateEngine;

	protected String htmlFromTemplatePedido(Pedido obj) {
		Context context = new Context();
		context.setVariable("pedido", obj);
		return templateEngine.process("email/confirmacaoPedido", context);
	}

	@Override
	public void sendOrderConfirmationHtmlEmail(Pedido obj) {
		try {
			MimeMessage mm = prepareMimeMessageFromPedido(obj);
			sendHtmlEmail(mm);
		}
		catch(MessagingException e) {
			sendOrderConfirmationEmail(obj);
		}

	}

	protected MimeMessage prepareMimeMessageFromPedido(Pedido obj) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper mmh = new MimeMessageHelper(mimeMessage, true);
		mmh.setTo(obj.getCliente().getEmail());
		mmh.setFrom(sender);
		mmh.setSubject("Pedido confirmado! Código: " + obj.getId());
		mmh.setSentDate(new Date(System.currentTimeMillis()));
		mmh.setText(htmlFromTemplatePedido(obj), true);

		return mimeMessage;
	}
~~~
(EmailService)
~~~JAVA
	void sendOrderConfirmationHtmlEmail(Pedido obj);

	void sendHtmlEmail(MimeMessage msg);
~~~
(MockEmailService)
~~~JAVA
@Override
	public void sendHtmlEmail(MimeMessage msg) {
		LOG.info("Simulando envio de email HTML...");
		LOG.info(msg.toString());
		LOG.info("Email enviado");

	}
~~~
(SmtpEmailService)
~~~JAVA
@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void sendHtmlEmail(MimeMessage msg) {
		LOG.info("Simulando envio de email...");
		javaMailSender.send(msg);
		LOG.info("Email enviado");

	}
~~~
(confirmacaoPedido)
~~~JAVA
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
	<title th:remove="all">Order Confirmation</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>

<body>
	<div>
		<h1>
			Pedido número: <span th:text="${pedido.id}"> </span>
		</h1>
		<p>
			Instante: <span th:text="${#dates.format(pedido.instante, 'dd/MM/yyyy hh:mm')}"></span>
		</p>
		<p>
			Cliente: <span th:text="${pedido.cliente.nome}"></span>
		</p>
		<p>
			Situação do pagamento: <span th:text="${pedido.pagamento.estado.descricao}"></span>
		</p>
		<h3>Detalhes do pedido:</h3>
		<table border="1">
			<tr>
				<th>Produto</th>
				<th>Quantidade</th>
				<th>Preço unitário</th>
				<th>Subtotal</th>
			</tr>
			<tr th:each="item : ${pedido.itens}">
				<td th:text="${item.produto.nome}">nome</td>
				<td th:text="${item.quantidade}">quantidade</td>
				<td th:text="${#numbers.formatDecimal(item.preco, 0, 'POINT', 2,
'COMMA')}">preco</td>
				<td th:text="${#numbers.formatDecimal(item.subTotal, 0, 'POINT', 2,
'COMMA')}">subTotal</td>
			</tr>
		</table>
		<p>
			Valor total: <span th:text="${#numbers.formatDecimal(pedido.valorTotal, 0,
'POINT', 2, 'COMMA')}"></span>
		</p>
	</div>
</body>

</html>
~~~

![emailHtml](https://user-images.githubusercontent.com/101072311/202929430-0733b7b1-42fe-4dbe-8f98-cf58b4c57246.png)

## Autenticação e Autorização com tokens JWT

#### Objetivo geral:

- Compreender o mecanismo de funcionamento do Spring Security
- Implementar autenticação e autorização com JWT
- Controlar conteúdo e acesso aos endpoints

### Implementando autenticacao e geracao do token JWT

(SecurityConfig)
~~~JAVA
@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JWTUtil jwtUtil;

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			http.headers().frameOptions().disable();
		}

		http.cors().and().csrf().disable();
		http.authorizeHttpRequests()
		.antMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
		.antMatchers(HttpMethod.GET, PUBLIC_MATCHERS_GET).permitAll()
		.antMatchers(PUBLIC_MATCHERS).permitAll().anyRequest().authenticated();
		http.addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtUtil));
		/*http.addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtUtil, userDetailsService));*/
	http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}
~~~
(CredenciaisDTO)
~~~JAVA
public class CredenciaisDTO  implements Serializable {
	private static final long serialVersionUID = 1L;

	private String email;
	private String senha;

	public CredenciaisDTO() {

	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}

}
~~~
(JWTAuthenticationFilter)
~~~JAVA
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	private JWTUtil jwtUtil;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
		setAuthenticationFailureHandler(new JWTAuthenticationFailureHandler());
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		try {
			CredenciaisDTO creds = new ObjectMapper().readValue(req.getInputStream(), CredenciaisDTO.class);

			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(creds.getEmail(),
					creds.getSenha(), new ArrayList<>());

			Authentication auth = authenticationManager.authenticate(authToken);
			return auth;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {
		String username = ((UserSS) auth.getPrincipal()).getUsername();
		String token = jwtUtil.generateToken(username);
		res.addHeader("Authorization", "Bearer " + token);

	}

	private class JWTAuthenticationFailureHandler implements AuthenticationFailureHandler {

		@Override
		public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException exception) throws IOException, ServletException {
			response.setStatus(401);
			response.setContentType("application/json");
			response.getWriter().append(json());
		}

		private String json() {
			long date = new Date().getTime();
			return "{\"timestamp\": " + date + ", " + "\"status\": 401, " + "\"error\": \"Não autorizado\", "
					+ "\"message\": \"Email ou senha inválidos\", " + "\"path\": \"/login\"}";
		}
	}
}
~~~
(JTWUtil)
~~~JAVA
@Component
public class JWTUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	public String generateToken(String username) {
		return Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(SignatureAlgorithm.HS512, secret.getBytes())
				.compact();
	}
}
~~~
(UserSS)
~~~JAVA
public class UserSS implements UserDetails{
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String email;
	private String senha;
	private Collection<? extends GrantedAuthority> authorities;

	public UserSS() {

	}

	public UserSS(Integer id, String email, String senha, Set<Perfil> perfis) {
		super();
		this.id = id;
		this.email = email;
		this.senha = senha;
		this.authorities = perfis.stream().map(x -> new SimpleGrantedAuthority(x.getDescricao())).collect(Collectors.toList());
	}

	public Integer getId() {
		return id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return  authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return senha;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {

		return true;
	}

	@Override
	public boolean isAccountNonLocked() {

		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		return true;
	}

	@Override
	public boolean isEnabled() {

		return true;
	}

}
~~~

### Implementando autorização

(SecurityConfig)
~~~JAVA

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			http.headers().frameOptions().disable();
		}

		http.cors().and().csrf().disable();
		http.authorizeHttpRequests()
		.antMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
		.antMatchers(HttpMethod.GET, PUBLIC_MATCHERS_GET).permitAll()
		.antMatchers(PUBLIC_MATCHERS).permitAll().anyRequest().authenticated();
		http.addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtUtil));
		http.addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtUtil, userDetailsService));
	http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}
~~~
(JWTAuthorizationFilter)
~~~JAVA
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private JWTUtil jwtUtil;

	private UserDetailsService userDetailsService;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
			UserDetailsService userDetailsService) {
		super(authenticationManager);
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

	String header = request.getHeader("Authorization");

		if(header != null && header.startsWith("Bearer ")) {
			UsernamePasswordAuthenticationToken auth = getAuthentication(header.substring(7));
			if(auth != null) {
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
		chain.doFilter(request, response);

	}

	private UsernamePasswordAuthenticationToken getAuthentication(String token) {
		if(jwtUtil.tokenValido(token)) {
			String username = jwtUtil.getUsername(token);
			UserDetails user = userDetailsService.loadUserByUsername(username);
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}
		return null;
	}
}
~~~
(JWTUtil)
~~~JAVA
public String generateToken(String username) {		
		return Jwts.builder().setSubject(username).setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(SignatureAlgorithm.HS512, secret.getBytes()).compact();
	}

	public boolean tokenValido(String token) {
		Claims claims = getClaims(token);
		if(claims != null) {
			String username = claims.getSubject();
			Date expirationDate = claims.getExpiration();
			Date now = new Date(System.currentTimeMillis());
			if(username != null && expirationDate != null && now.before(expirationDate)) {
				return true;
			}

		}
		return false;
	}


	public String getUsername(String token) {
		Claims claims = getClaims(token);
		if(claims != null) {
			return claims.getSubject();
		}
		return null;
	}


	private Claims getClaims(String token) {
		try {
		return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
		}  
		catch(Exception e) {
			return null;
		}
	}

~~~

### Autorizando endpoints para perfis específicos

(SecurityConfig)
~~~JAVA
	private static final String[] PUBLIC_MATCHERS_GET = { "/produtos/**", "/categorias/**"};

	private static final String[] PUBLIC_MATCHERS_POST = { "/clientes/**"};
~~~
(CategoriaResource) Anotação nos métodos POST/DELETE/PUT
~~~JAVA
@PreAuthorize("hasAnyRole('ADMIN')")
~~~
(ResourceExceptionHandler)
~~~JAVA
@ExceptionHandler(AuthorizationException.class)
	public ResponseEntity<StandardError> authorization(AuthorizationException e, HttpServletRequest request) {

		StandardError err = new StandardError(HttpStatus.FORBIDDEN.value(), e.getMessage(), System.currentTimeMillis());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
	}
~~~
(UserSS)
~~~JAVA
public boolean hasRole(Perfil perfil) {
		return getAuthorities().contains(new SimpleGrantedAuthority(perfil.getDescricao()));
	}
~~~
(clienteService)
~~~JAVA
public Cliente find(Integer id) {
		 Optional<Cliente> obj = repo.findById(id);
		UserSS user = UserService.authenticated();
		if(user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}

		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
		 "Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
		}
~~~
(UserService)
~~~JAVA
public class UserService {
	public static UserSS authenticated() {
		try {
			return (UserSS) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		} catch (Exception e) {
			return null;
		}
	}
}
~~~
(AuthorizationException)
~~~JAVA
public class AuthorizationException  extends RuntimeException{


	private static final long serialVersionUID = 1L;

	public AuthorizationException(String msg) {
		super(msg);
	}

	public AuthorizationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
~~~
(PedidoRepository)
~~~JAVA
@Transactional(readOnly=true)
	Page<Pedido> findByCliente(Cliente cliente, Pageable pageRequest);
~~~
(PedidoResource)
~~~JAVA
@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<Page<Pedido>> findPage(
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			@RequestParam(value = "linesPerPage", defaultValue = "24") Integer linesPerPage,
			@RequestParam(value = "orderBy", defaultValue = "instante") String orderBy,
			@RequestParam(value = "direction", defaultValue = "DESC") String direction) {

		Page<Pedido> list = service.findPage(page, linesPerPage, orderBy, direction);

		return ResponseEntity.ok().body(list);

	}
~~~
(PedidoService)
~~~JAVA
public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		UserSS user = UserService.authenticated();
		if(user == null) {
			throw new AuthorizationException("Acesso negado");
		}
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction),orderBy);
		Cliente cliente =  clienteService.find(user.getId());
		return repo.findByCliente(cliente, pageRequest);
	}
~~~

### Refresh token

(AuthResource)
~~~JAVA
@RestController
@RequestMapping(value= "/auth")
public class AuthResource {

	@Autowired
	private JWTUtil jwtUtil;

	@RequestMapping(value="/refresh_token", method= RequestMethod.POST)
	public ResponseEntity<Void> refreshToken(HttpServletResponse response) {
	UserSS user = UserService.authenticated();
	String token = jwtUtil.generateToken(user.getUsername());
	response.addHeader("Authorization", "Bearer " + token);
	return ResponseEntity.noContent().build();
	}

}
~~~
(SecurityConfig)
~~~JAVA
private static final String[] PUBLIC_MATCHERS_POST = { "/clientes/**", "/auth/forgot**"};
~~~
(EmailDTO)
~~~JAVA
public class EmailDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotEmpty(message="Preenchimento obrigatório")
	@Email(message="Email inválido")
	private String Email;

	public EmailDTO() {

	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}


}
~~~
(AuthResource)
~~~JAVA
@Autowired
	private AuthService service;

	@RequestMapping(value="/forgot", method= RequestMethod.POST)
	public ResponseEntity<Void> forgot(@Valid @RequestBody EmailDTO objDto) {
		service.sendNewPassword(objDto.getEmail());

	return ResponseEntity.noContent().build();
	}
~~~
(AbstractEmailService)
~~~JAVA
@Override
	public void sendNewPasswordEmail(Cliente cliente, String newPass) {
		SimpleMailMessage sm = prepareNewPasswordEmail(cliente, newPass);
		sendEmail(sm);
	}

	protected SimpleMailMessage prepareNewPasswordEmail(Cliente cliente, String newPass) {
		SimpleMailMessage sm = new SimpleMailMessage();
		sm.setTo(cliente.getEmail());
		sm.setFrom(sender);
		sm.setSubject("Solicitação de nova senha");
		sm.setSentDate(new Date(System.currentTimeMillis()));
		sm.setText("Nova senha: " + newPass);
		return sm;

	}
~~~
(AuthService)
~~~JAVA
@Service
public class AuthService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private BCryptPasswordEncoder pe;

	@Autowired
	private EmailService emailService;

	private Random rand = new Random();

	public void sendNewPassword(String email) {

		Cliente cliente = clienteRepository.findByEmail(email);
		if (cliente == null) {
			throw new ObjectNotFoundException("Email não encontrado");
		}

		String newPass = newPassword();
		cliente.setSenha(pe.encode(newPass));

		clienteRepository.save(cliente);
		emailService.sendNewPasswordEmail(cliente, newPass);
	}

	private String newPassword() {
		char[] vet = new char[10];
		for (int i=0; i<10; i++) {
			vet[i] = randomChar();
		}
		return new String(vet);
	}

	private char randomChar() {
		int opt = rand.nextInt(3);
		if (opt == 0) { // gera um digito
			return (char) (rand.nextInt(10) + 48);
		}
		else if (opt == 1) { // gera letra maiuscula
			return (char) (rand.nextInt(26) + 65);
		}
		else { // gera letra minuscula
			return (char) (rand.nextInt(26) + 97);
		}
	}
}
~~~
## Ferramentas e Tecnologias usadas nesse repositório 🧱
<div style="display: inline_block"><br>


<img align="center" alt="Augusto-Java" height="60" width="60" src=https://github.com/devicons/devicon/blob/master/icons/java/java-original.svg >
<img align="center" alt="Augusto-SpringBoot" height="60" width="60" src="https://raw.githubusercontent.com/devicons/devicon/1119b9f84c0290e0f0b38982099a2bd027a48bf1/icons/spring/spring-original-wordmark.svg">
<img align="center" alt="Augusto-H2" height="50" width="50" src="https://user-images.githubusercontent.com/101072311/200666111-2e4878bb-7d5c-4103-a159-fd00d0855a5d.png">

</div>    

### Teste o projeto 👁‍🗨

Download do projeto para testar em sua máquina: https://github.com/AugustoMello09/cursoSpringBackend/archive/refs/heads/master.zip

## Entre em contato comigo através dos canais abaixo e desde já, agradeço a atenção. 🤝

<div>

  <a href="https://www.linkedin.com/in/jos%C3%A9-augusto-mello-794a94234" target="_blank"><img src="https://img.shields.io/badge/-LinkedIn-%230077B5?style=for-the-badge&logo=linkedin&logoColor=white" target="_blank"></a>
   <a href="mailto:joseaugusto.Mello01@gmail.com" target="_blank"><img src="https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white" target="_blank"></a>

  </div>

