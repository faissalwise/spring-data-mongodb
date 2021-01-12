/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mongodb.core.index;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Arrays;

import org.bson.Document;
import org.junit.Test;
import org.springframework.data.domain.Sort.Direction;

/**
 * Unit tests for {@link IndexInfo}.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 */
public class IndexInfoUnitTests {

	static final String ID_INDEX = "{ \"v\" : 2, \"key\" : { \"_id\" : 1 }, \"name\" : \"_id_\", \"ns\" : \"db.collection\" }";
	static final String INDEX_WITH_PARTIAL_FILTER = "{ \"v\" : 2, \"key\" : { \"k3y\" : 1 }, \"name\" : \"partial-filter-index\", \"ns\" : \"db.collection\", \"partialFilterExpression\" : { \"quantity\" : { \"$gte\" : 10 } } }";
	static final String INDEX_WITH_EXPIRATION_TIME = "{ \"v\" : 2, \"key\" : { \"lastModifiedDate\" : 1 },\"name\" : \"expire-after-last-modified\", \"ns\" : \"db.collectio\", \"expireAfterSeconds\" : 3600 }";
	static final String HASHED_INDEX = "{ \"v\" : 2, \"key\" : { \"score\" : \"hashed\" }, \"name\" : \"score_hashed\", \"ns\" : \"db.collection\" }";

	@Test
	public void isIndexForFieldsCorrectly() {

		IndexField fooField = IndexField.create("foo", Direction.ASC);
		IndexField barField = IndexField.create("bar", Direction.DESC);

		IndexInfo info = new IndexInfo(Arrays.asList(fooField, barField), "myIndex", false, false, "");
		assertThat(info.isIndexForFields(Arrays.asList("foo", "bar"))).isTrue();
	}

	@Test // DATAMONGO-2170
	public void partialFilterExpressionShouldBeNullIfNotSetInSource() {
		assertThat(getIndexInfo(ID_INDEX).getPartialFilterExpression()).isNull();
	}

	@Test // DATAMONGO-2170
	public void partialFilterExpressionShouldMatchSource() {

		assertThat(Document.parse(getIndexInfo(INDEX_WITH_PARTIAL_FILTER).getPartialFilterExpression()))
				.isEqualTo(Document.parse("{ \"quantity\" : { \"$gte\" : 10 } }"));
	}

	@Test // DATAMONGO-2081
	public void expireAfterIsParsedCorrectly() {
		assertThat(getIndexInfo(INDEX_WITH_EXPIRATION_TIME).getExpireAfter()).contains(Duration.ofHours(1));
	}

	@Test // DATAMONGO-2081
	public void expireAfterIsEmptyIfNotSet() {
		assertThat(getIndexInfo(ID_INDEX).getExpireAfter()).isEmpty();
	}

	@Test // DATAMONGO-1183
	public void readsHashedIndexCorrectly() {
		assertThat(getIndexInfo(HASHED_INDEX).getIndexFields()).containsExactly(IndexField.hashed("score"));
	}

	@Test // DATAMONGO-1183
	public void hashedIndexIsMarkedAsSuch() {
		assertThat(getIndexInfo(HASHED_INDEX).isHashed()).isTrue();
	}

	private static IndexInfo getIndexInfo(String documentJson) {
		return IndexInfo.indexInfoOf(Document.parse(documentJson));
	}
}
