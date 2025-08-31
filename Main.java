import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

// Enumeração para as categorias de eventos
enum CategoriaEvento {
    FESTA,
    SHOW,
    ESPORTIVO,
    CULTURAL,
    OUTRO
}

// Classe que representa um usuário
class Usuario implements Serializable {
    private String nome;
    private String email;
    private String cidade;
    private List<Evento> eventosConfirmados;

    public Usuario(String nome, String email, String cidade) {
        this.nome = nome;
        this.email = email;
        this.cidade = cidade;
        this.eventosConfirmados = new ArrayList<>();
    }

    // Getters
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCidade() { return cidade; }

    public List<Evento> getEventosConfirmados() {
        return eventosConfirmados;
    }

    // Métodos para gerenciar a participação em eventos
    public void adicionarEvento(Evento evento) {
        if (!eventosConfirmados.contains(evento)) {
            eventosConfirmados.add(evento);
        }
    }

    public void removerEvento(Evento evento) {
        eventosConfirmados.remove(evento);
    }
    
    @Override
    public String toString() {
        return "Nome: " + nome + ", Email: " + email;
    }
}

// Classe que representa um evento
class Evento implements Serializable {
    private String nome;
    private String endereco;
    private CategoriaEvento categoria;
    private LocalDateTime horario;
    private String descricao;
    private List<Usuario> participantes;

    public Evento(String nome, String endereco, CategoriaEvento categoria, LocalDateTime horario, String descricao) {
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.horario = horario;
        this.descricao = descricao;
        this.participantes = new ArrayList<>();
    }

    // Getters e Setters (adicionados para permitir a edição)
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public CategoriaEvento getCategoria() { return categoria; }
    public void setCategoria(CategoriaEvento categoria) { this.categoria = categoria; }

    public LocalDateTime getHorario() { return horario; }
    public void setHorario(LocalDateTime horario) { this.horario = horario; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<Usuario> getParticipantes() { return participantes; }

    // Métodos para gerenciar participantes
    public void adicionarParticipante(Usuario usuario) {
        if (!participantes.contains(usuario)) {
            participantes.add(usuario);
        }
    }

    public void removerParticipante(Usuario usuario) {
        participantes.remove(usuario);
    }

    // Métodos para verificar o status do evento
    public boolean isOcorrendoAgora() {
        LocalDateTime agora = LocalDateTime.now();
        return agora.isAfter(horario) && agora.isBefore(horario.plus(2, ChronoUnit.HOURS));
    }

    public boolean isJaOcorreu() {
        return LocalDateTime.now().isAfter(horario.plus(2, ChronoUnit.HOURS));
    }
    
    @Override
    public String toString() {
        return "Nome: " + nome +
               "\n  Endereço: " + endereco +
               "\n  Categoria: " + categoria +
               "\n  Horário: " + horario.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")) +
               "\n  Descrição: " + descricao + "\n";
    }
}

// Classe principal que contém a lógica do sistema
public class Main {
    private static List<Evento> eventos = new ArrayList<>();
    private static Usuario usuarioLogado;
    private static final String NOME_ARQUIVO = "events.data";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        carregarEventos();
        Scanner scanner = new Scanner(System.in);
        int opcao;

        if (usuarioLogado == null) {
            cadastrarUsuario(scanner);
        }

        do {
            System.out.println("\n--- Sistema de Eventos ---");
            System.out.println("1. Listar eventos disponíveis");
            System.out.println("2. Cadastrar novo evento");
            System.out.println("3. Ver meus eventos confirmados");
            System.out.println("4. Editar evento");
            System.out.println("5. Excluir evento");
            System.out.println("6. Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    listarEventos(scanner);
                    break;
                case 2:
                    cadastrarEvento(scanner);
                    break;
                case 3:
                    listarEventosConfirmados(scanner);
                    break;
                case 4:
                    editarEvento(scanner);
                    break;
                case 5:
                    excluirEvento(scanner);
                    break;
                case 6:
                    System.out.println("Salvando eventos e saindo...");
                    salvarEventos();
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 6);

        scanner.close();
    }
    
    // --- Métodos de Interação com o Usuário ---
    
    private static void cadastrarUsuario(Scanner scanner) {
        System.out.println("\n--- Cadastro de Usuário ---");
        System.out.print("Digite seu nome: ");
        String nome = scanner.nextLine();
        System.out.print("Digite seu email: ");
        String email = scanner.nextLine();
        System.out.print("Digite sua cidade: ");
        String cidade = scanner.nextLine();
        
        usuarioLogado = new Usuario(nome, email, cidade);
        System.out.println("Usuário " + nome + " cadastrado com sucesso!");
    }

    private static void listarEventos(Scanner scanner) {
        System.out.println("\n--- Eventos na sua cidade ---");
        
        eventos.sort(Comparator.comparing(Evento::getHorario));

        for (int i = 0; i < eventos.size(); i++) {
            Evento evento = eventos.get(i);
            String status = "";
            if (evento.isOcorrendoAgora()) {
                status = " (OCORRENDO AGORA)";
            } else if (evento.isJaOcorreu()) {
                status = " (JÁ OCORREU)";
            }
            System.out.println((i + 1) + ". " + evento.getNome() + " - " + evento.getHorario().format(FORMATO_DATA) + status);
        }

        System.out.print("\nDigite o número do evento para ver detalhes ou 0 para voltar: ");
        int escolha = scanner.nextInt();
        scanner.nextLine();
        
        if (escolha > 0 && escolha <= eventos.size()) {
            Evento eventoEscolhido = eventos.get(escolha - 1);
            System.out.println(eventoEscolhido);
            
            System.out.print("Deseja confirmar sua participação neste evento? (s/n): ");
            String confirmacao = scanner.nextLine();
            if (confirmacao.equalsIgnoreCase("s")) {
                usuarioLogado.adicionarEvento(eventoEscolhido);
                eventoEscolhido.adicionarParticipante(usuarioLogado);
                System.out.println("Participação confirmada com sucesso!");
            }
        }
    }

    private static void cadastrarEvento(Scanner scanner) {
        System.out.println("\n--- Cadastro de Novo Evento ---");
        System.out.print("Nome do evento: ");
        String nome = scanner.nextLine();
        System.out.print("Endereço: ");
        String endereco = scanner.nextLine();
        
        System.out.println("Categorias disponíveis: " + java.util.Arrays.toString(CategoriaEvento.values()));
        System.out.print("Categoria: ");
        CategoriaEvento categoria = CategoriaEvento.valueOf(scanner.nextLine().toUpperCase());
        
        System.out.print("Horário (dd/MM/yyyy HH:mm): ");
        String horarioString = scanner.nextLine();
        LocalDateTime horario = LocalDateTime.parse(horarioString, FORMATO_DATA);
        
        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();
        
        Evento novoEvento = new Evento(nome, endereco, categoria, horario, descricao);
        eventos.add(novoEvento);
        System.out.println("Evento cadastrado com sucesso!");
    }

    private static void listarEventosConfirmados(Scanner scanner) {
        System.out.println("\n--- Meus Eventos Confirmados ---");
        List<Evento> eventosConfirmados = usuarioLogado.getEventosConfirmados();

        if (eventosConfirmados.isEmpty()) {
            System.out.println("Você ainda não confirmou presença em nenhum evento.");
            return;
        }

        for (int i = 0; i < eventosConfirmados.size(); i++) {
            Evento evento = eventosConfirmados.get(i);
            System.out.println((i + 1) + ". " + evento.getNome() + " - " + evento.getHorario().format(FORMATO_DATA));
        }

        System.out.print("\nDigite o número do evento para cancelar a participação ou 0 para voltar: ");
        int escolha = scanner.nextInt();
        scanner.nextLine();

        if (escolha > 0 && escolha <= eventosConfirmados.size()) {
            Evento eventoParaRemover = eventosConfirmados.get(escolha - 1);
            usuarioLogado.removerEvento(eventoParaRemover);
            eventoParaRemover.removerParticipante(usuarioLogado);
            System.out.println("Participação cancelada com sucesso!");
        }
    }

    private static void editarEvento(Scanner scanner) {
        System.out.println("\n--- Editar Evento ---");
        if (eventos.isEmpty()) {
            System.out.println("Não há eventos para editar.");
            return;
        }

        for (int i = 0; i < eventos.size(); i++) {
            System.out.println((i + 1) + ". " + eventos.get(i).getNome());
        }

        System.out.print("\nDigite o número do evento que deseja editar ou 0 para voltar: ");
        int escolha = scanner.nextInt();
        scanner.nextLine();

        if (escolha > 0 && escolha <= eventos.size()) {
            Evento eventoParaEditar = eventos.get(escolha - 1);
            System.out.println("\nEvento selecionado: " + eventoParaEditar.getNome());

            System.out.println("Qual atributo deseja editar?");
            System.out.println("1. Nome");
            System.out.println("2. Endereço");
            System.out.println("3. Categoria");
            System.out.println("4. Horário");
            System.out.println("5. Descrição");
            System.out.print("Escolha uma opção: ");
            int atributo = scanner.nextInt();
            scanner.nextLine();

            switch (atributo) {
                case 1:
                    System.out.print("Novo nome: ");
                    eventoParaEditar.setNome(scanner.nextLine());
                    break;
                case 2:
                    System.out.print("Novo endereço: ");
                    eventoParaEditar.setEndereco(scanner.nextLine());
                    break;
                case 3:
                    System.out.println("Categorias disponíveis: " + java.util.Arrays.toString(CategoriaEvento.values()));
                    System.out.print("Nova categoria: ");
                    eventoParaEditar.setCategoria(CategoriaEvento.valueOf(scanner.nextLine().toUpperCase()));
                    break;
                case 4:
                    System.out.print("Novo horário (dd/MM/yyyy HH:mm): ");
                    eventoParaEditar.setHorario(LocalDateTime.parse(scanner.nextLine(), FORMATO_DATA));
                    break;
                case 5:
                    System.out.print("Nova descrição: ");
                    eventoParaEditar.setDescricao(scanner.nextLine());
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
            System.out.println("Evento editado com sucesso!");
        }
    }

    private static void excluirEvento(Scanner scanner) {
        System.out.println("\n--- Excluir Evento ---");
        if (eventos.isEmpty()) {
            System.out.println("Não há eventos para excluir.");
            return;
        }

        for (int i = 0; i < eventos.size(); i++) {
            System.out.println((i + 1) + ". " + eventos.get(i).getNome());
        }

        System.out.print("\nDigite o número do evento que deseja excluir ou 0 para voltar: ");
        int escolha = scanner.nextInt();
        scanner.nextLine();

        if (escolha > 0 && escolha <= eventos.size()) {
            Evento eventoParaExcluir = eventos.get(escolha - 1);
            eventos.remove(eventoParaExcluir);
            // Também remove o evento da lista de confirmados de cada usuário
            for (Usuario u : eventoParaExcluir.getParticipantes()) {
                u.removerEvento(eventoParaExcluir);
            }
            System.out.println("Evento '" + eventoParaExcluir.getNome() + "' excluído com sucesso!");
        }
    }

    // --- Métodos de Persistência de Dados ---

    private static void salvarEventos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOME_ARQUIVO))) {
            oos.writeObject(eventos);
        } catch (IOException e) {
            System.err.println("Erro ao salvar eventos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void carregarEventos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NOME_ARQUIVO))) {
            eventos = (List<Evento>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo de eventos não encontrado. Iniciando com uma lista vazia.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar eventos: " + e.getMessage());
            eventos = new ArrayList<>();
        }
    }
}