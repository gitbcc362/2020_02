/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spendreport;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.walkthrough.common.entity.Alert;
import org.apache.flink.walkthrough.common.entity.Transaction;

/**
 * FraudDetector define a logica para detectar fraudes
 */
public class FraudDetector extends KeyedProcessFunction<Long, Transaction, Alert> {

	private static final long serialVersionUID = 1L;

	private static final double SMALL_AMOUNT = 1.00;
	private static final double LARGE_AMOUNT = 500.00;
	private static final long ONE_MINUTE = 60 * 1000;

	/**
	 *  O Tipo ValueState é um tipo de dado que adiciona tolerancia a falha a toda variavel qeu for adicionado
	 *  Está somente disponivel para operators que usam KeyedContext.
	 *  Há tres metodos para interação com os conteudos: update (seta o estado), value (obtem o estado atual) e clear (deleta os conteudos)
	 */
	private transient ValueState<Boolean> flagState;

	/**
	 * ValueStateDescriptor contem metadados de como o Flink deve tratar a variavel.
	 * O estado deve ser obtido antse da função processar os dados, e isso é feito com o metodo Open()
	 */
	@Override
	public void open(Configuration parameters) {
		ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
				"flag",
				Types.BOOLEAN);
		flagState = getRuntimeContext().getState(flagDescriptor);
	}

	/**
	 * Esse metodo é chamado para cada evento de transação.
	 */
	@Override
	public void processElement(Transaction transaction, Context context, Collector<Alert> collector) throws Exception {

		// Obtem o estado atul para o id atual
		Boolean lastTransactionWasSmall = flagState.value();

		// verifica se a flag foi acionada
		if (lastTransactionWasSmall != null) {
			if (transaction.getAmount() > LARGE_AMOUNT) {
				// gera um alerta
				Alert alert = new Alert();
				alert.setId(transaction.getAccountId());

				collector.collect(alert);
			}

			// atualiza o estado
			flagState.clear();
		}

		if (transaction.getAmount() < SMALL_AMOUNT) {
			// atualiza o estado
			flagState.update(true);
		}
	}
}
