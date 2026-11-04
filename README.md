# API Hospitalar — Sprint 2 (Spring Boot + MongoDB)

## Rodando
```bash
docker compose up -d      # sobe o MongoDB em localhost:27017
./mvnw spring-boot:run    # ou: mvn spring-boot:run
```
Java 17+. Instale o plugin do Lombok na IDE.

## Endpoints
| Recurso | Rotas |
|---|---|
| `/pacientes` | POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}`, GET `/{id}/historico` |
| `/profissionais` | POST, GET (`?especialidade=CARDIOLOGIA`), GET/PUT/DELETE `/{id}` |
| `/profissionais/{id}/procedimentos` | POST, GET, GET/PUT `/{procId}`, PATCH `/{procId}/situacao` |
| `/consultas` | POST, GET (`?pacienteId=`), GET `/{id}`, PUT `/{id}`, PATCH `/{id}/situacao` |
| `/internacoes` | POST, GET (`?pacienteId=`), GET `/{id}`, PUT `/{id}`, PATCH `/{id}/alta` |
| `/quartos` | POST, GET (`?disponiveis=true`), GET/PUT/DELETE `/{id}` |

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

# Quarto + internação + alta
curl -X POST localhost:8080/quartos -H 'Content-Type: application/json' -d '{
  "numId":"101","andar":1,"capacidadeMax":2,"tipo":"ENFERMARIA"}'
curl -X POST localhost:8080/internacoes -H 'Content-Type: application/json' -d '{
  "pacienteId":"<ID>","profissionalId":"<ID>","quartoId":"<ID>",
  "dataEntrada":"2026-11-10","dataPrevistaAlta":"2026-11-15"}'
curl -X PATCH localhost:8080/internacoes/<ID>/alta -H 'Content-Type: application/json' \
  -d '{"data":"2026-11-14"}'
```

## Erros
`404` recurso não encontrado · `422` regra de negócio violada · `400` JSON/validação inválidos.
