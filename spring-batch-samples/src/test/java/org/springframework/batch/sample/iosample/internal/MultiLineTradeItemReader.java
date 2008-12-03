/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.sample.iosample.internal;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSet;
import org.springframework.batch.sample.domain.trade.Trade;
import org.springframework.util.Assert;

/**
 * @author Dan Garrette
 * @since 2.0
 */
public class MultiLineTradeItemReader implements ItemReader<Trade>, ItemStream {

	private FlatFileItemReader<FieldSet> delegate;

	/**
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	public Trade read() throws Exception {
		Trade t = null;

		for (FieldSet line = null; (line = this.delegate.read()) != null;) {
			String prefix = line.readString(0);
			if (prefix.equals("BEGIN")) {
				t = new Trade(); // Record must start with 'BEGIN'
			}
			else if (prefix.equals("INFO")) {
				Assert.notNull(t, "No 'BEGIN' was found.");
				t.setIsin(line.readString(1));
				t.setCustomer(line.readString(2));
			}
			else if (prefix.equals("AMNT")) {
				Assert.notNull(t, "No 'BEGIN' was found.");
				t.setQuantity(line.readInt(1));
				t.setPrice(line.readBigDecimal(2));
			}
			else if (prefix.equals("END")) {
				return t; // Record must end with 'END'
			}
		}
		Assert.isNull(t, "No 'END' was found.");
		return null;
	}

	public void setDelegate(FlatFileItemReader<FieldSet> delegate) {
		this.delegate = delegate;
	}

	public void close(ExecutionContext executionContext) throws ItemStreamException {
		this.delegate.close(executionContext);
	}

	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.delegate.open(executionContext);
	}

	public void update(ExecutionContext executionContext) throws ItemStreamException {
		this.delegate.update(executionContext);
	}
}