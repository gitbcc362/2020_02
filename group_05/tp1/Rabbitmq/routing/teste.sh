#!/bin/bash
echo "Iniciando teste"
node emitter.js error "mensagem de error"
node emitter.js warning "mensagem de warning"
node emitter.js error "mensagem de error"
node emitter.js warning "mensagem de warning"
echo "finalizando teste"
