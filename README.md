# API Hospitalar — Sprint 2 (Spring Boot + MongoDB)

## Rodando
```bash
docker compose up -d      # sobe o MongoDB em localhost:27017
./mvnw spring-boot:run    # ou: mvn spring-boot:run
```
Java 17+. Instale o plugin do Lombok na IDE.

## Estrutura
```
pom.xml                 # único pom (Spring Boot 3.3.5 + MongoDB)
src/main/java/br/pucminas/hospital/{model,repository,service,controller,dto,exception}
src/main/resources/     # application.properties + telas estáticas
docs/                   # diagramas de classes e cartões CRC
docker-compose.yml      # MongoDB local
```

## Endpoints
| Recurso | Rotas |
|---|---|
| `/pacientes` | POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}`, GET `/{id}/historico` |
| `/profissionais` | POST, GET (`?tipo=ESPECIALISTA&especialidade=CARDIOLOGIA`), GET/PUT/DELETE `/{id}`, GET `/disponiveis?data=2026-11-20&horario=14:00` (aceita os mesmos filtros), GET `/{id}/consultas` (`?situacao=CONCLUIDA`) |
| `/profissionais/{id}/procedimentos` | POST, GET, GET/PUT `/{procId}` (aceita `descricaoProcedimento`, `tipoCirurgia`, `observacoesPreOp`, `observacoesPosOp`), PATCH `/{procId}/situacao` |
| `/consultas` | POST, GET (`?pacienteId=&tipo=EMERGENCIA&situacao=AGUARDANDO_AVALIACAO`), GET `/emergencias` (`?situacao=`, ordenado por prioridade), GET `/{id}`, PUT `/{id}`, PATCH `/{id}/situacao`, POST `/{id}/reagendamento`, POST `/{id}/encaminhamento` |
| `/internacoes` | POST, GET (`?pacienteId=`), GET `/{id}`, PUT `/{id}`, PATCH `/{id}/alta` |
| `/quartos` | POST, GET (`?tipo=UTI&disponiveis=true`), GET/PUT/DELETE `/{id}` |

O campo `"tipo"` define o subtipo:
- Profissional: `CLINICO_GERAL` | `ESPECIALISTA` | `CIRURGIAO`
- Atendimento: `ROTINA` | `ESPECIALIZADA` | `EMERGENCIA` | `CIRURGICO`

## Exemplos
```bash
# Paciente
curl -X POST localhost:8080/pacientes -H 'Content-Type: application/json' -d '{
  "nome":"Maria Silva","cpf":"123.456.789-00","dataDeNascimento":"1990-05-10",
  "telefone":"31999990000","endereco":"Rua A, 10","email":"maria@email.com"}'

# Clínico geral
curl -X POST localhost:8080/profissionais -H 'Content-Type: application/json' -d '{
  "tipo":"CLINICO_GERAL","nome":"Dr. João","registroProfissional":"CRM-MG 1234",
  "telefone":"3133330000","email":"joao@hospital.com"}'

# Consulta de rotina (troque os ids)
curl -X POST localhost:8080/consultas -H 'Content-Type: application/json' -d '{
  "tipo":"ROTINA","pacienteId":"<ID>","profissionalId":"<ID>",
  "data":"2026-11-10","horario":"09:00","motivo":"Check-up"}'

# Mudar situação
curl -X PATCH localhost:8080/consultas/<ID>/situacao -H 'Content-Type: application/json' \
  -d '{"situacao":"CONFIRMADA"}'

# Clínico geral encaminha para um especialista (consulta de rotina em atendimento ou concluída)
curl -X POST localhost:8080/consultas/<ID>/encaminhamento -H 'Content-Type: application/json' -d '{
  "especialidade":"CARDIOLOGIA","profissionalId":"<ID_ESPECIALISTA>","data":"2026-11-20","horario":"14:00"}'

# Nova data para uma consulta cancelada ou com falta (cria outra consulta, ligada à anterior)
curl -X POST localhost:8080/consultas/<ID>/reagendamento -H 'Content-Type: application/json' \
  -d '{"data":"2026-11-25","horario":"10:00"}'

# Fila de emergências que aguardam avaliação, da mais urgente para a menos urgente
curl 'localhost:8080/consultas/emergencias?situacao=AGUARDANDO_AVALIACAO'

# Quarto + internação + alta
curl -X POST localhost:8080/quartos -H 'Content-Type: application/json' -d '{
  "numId":"101","andar":1,"capacidadeMax":2,"tipo":"ENFERMARIA"}'
curl -X POST localhost:8080/internacoes -H 'Content-Type: application/json' -d '{
  "pacienteId":"<ID>","profissionalId":"<ID>","quartoId":"<ID>",
  "dataEntrada":"2026-11-10","dataPrevistaAlta":"2026-11-15"}'
curl -X PATCH localhost:8080/internacoes/<ID>/alta -H 'Content-Type: application/json' \
  -d '{"data":"2026-11-14"}'
```

## Regras de negócio
- Um profissional não pode ter dois atendimentos ativos no mesmo horário (cancelados e faltas liberam o horário).
- Consulta de rotina exige clínico geral; consulta especializada exige especialista da mesma área; procedimento exige cirurgião, descrição e tipo de cirurgia; emergência exige prioridade.
- Situações seguem transições fixas (ex.: concluída não volta a agendada). Consulta cancelada ou com falta só volta a acontecer por `/reagendamento`, que cria um novo agendamento.
- Data e horário só mudam antes do início do atendimento; observações só são registradas a partir do início. No procedimento, pré-operatório só antes do início e pós-operatório só depois.
- Só o clínico geral encaminha, a partir de uma consulta de rotina em atendimento ou concluída.
- Profissional com atendimentos não pode mudar de categoria nem de especialidade.
- No procedimento, descrição e tipo de cirurgia só podem ser alterados enquanto ele está agendado.
- Tipos de quarto: **enfermaria** é coletiva (2 a 6 leitos); **apartamento** e **UTI** são individuais (1 leito). A regra vale no cadastro e na alteração do quarto.
- Na alta, a internação é salva antes de o leito ser liberado; se a liberação falhar, a alta é desfeita.

## Modelagem
O diagrama de classes da Sprint 2, gerado a partir do código, está em [`docs/diagramas/diagrama-de-classes-sprint2.png`](docs/diagramas/diagrama-de-classes-sprint2.png). A fonte em PlantUML fica ao lado (`.puml`), para atualizar sempre que o modelo mudar.

- `ConRotina` e `ConEspecializada` herdam de `Consulta`, que concentra a situação, as transições e o reagendamento, sem duplicar código.
- Cada modalidade implementa a própria validação (`validarCondicoes`) e define quando aceita mudança de agenda e registro de observações.

## Por que MongoDB
O núcleo do domínio são duas hierarquias: profissionais (clínico geral, especialista, cirurgião) e atendimentos (rotina, especializada, emergência, procedimento cirúrgico). Cada subtipo tem campos próprios, como especialidade, prioridade e observações pré e pós-operatórias. Num banco orientado a documentos, cada hierarquia fica em **uma coleção só**: o Spring Data grava o tipo de cada documento e devolve a subclasse certa ao ler. Assim não é preciso escolher entre várias tabelas com junções ou uma tabela larga cheia de colunas vazias, e listar "todos os atendimentos de um paciente" vira uma consulta simples numa coleção.

O modelo também evolui a cada sprint, com novas modalidades e novos campos por tipo. Documentos sem esquema fixo aceitam isso sem migração de banco. Os pontos em que precisamos de consistência são tratados na aplicação: transições de situação validadas no modelo, conflito de horário verificado antes de salvar e controle otimista de concorrência (`@Version`) na ocupação dos quartos.

## Erros
`404` recurso não encontrado · `422` regra de negócio violada · `400` JSON/validação inválidos ou dado obrigatório ausente (ex.: paciente, profissional ou quarto não informado).
