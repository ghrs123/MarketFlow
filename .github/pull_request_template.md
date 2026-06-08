## Objetivo

<!-- What becomes possible after this PR that was not possible before? -->

## Funcionalidade entregue

<!-- Describe the complete vertical slice delivered in this PR. -->

## O que foi implementado

- 
- 
- 

## Conceitos estudados

- 
- 
- 

## Como executar

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow

mvn spring-boot:run
```

## Como testar

```bash
mvn test
mvn package
```

## Exemplos curl

```bash
# Add curl commands for the endpoints or flows covered by this PR.
```

## Logs esperados

<!-- Note the key business or technical log lines expected in a successful run. -->

## Métricas esperadas

<!-- List relevant actuator or Micrometer metrics expected for this change. -->

## Trade-offs técnicos

- 
- 

## Riscos conhecidos

- 
- 

## Screenshots ou evidências

<!-- Paste Swagger screenshots, test output summaries, CI links, or terminal evidence. -->

## Perguntas de entrevista relacionadas

1. 
2. 
3. 
4. 
5. 

## Checklist

- [ ] Código compila
- [ ] Testes passam localmente
- [ ] Funcionalidade principal pode ser demonstrada end-to-end
- [ ] Sem secrets hardcoded
- [ ] Sem stack traces ou mensagens internas expostas ao cliente
- [ ] Logs relevantes usam formato parametrizado
- [ ] Código está na camada arquitetural correta
- [ ] README atualizado se endpoints ou env vars mudaram
- [ ] Documentação da fase criada ou atualizada
- [ ] CI passa

## Próximos passos

- 
- 
