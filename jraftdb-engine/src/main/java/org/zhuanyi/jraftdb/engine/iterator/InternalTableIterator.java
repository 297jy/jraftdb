/*
 * Copyright (C) 2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zhuanyi.jraftdb.engine.iterator;

import com.google.common.collect.Maps;
import org.zhuanyi.jraftdb.engine.utils.slice.Slice;
import org.zhuanyi.jraftdb.engine.dto.InternalKey;


import java.util.Map.Entry;

public class InternalTableIterator
        extends AbstractSeekingIterator<InternalKey, Slice>
        implements InternalIterator {
    private final TableIterator tableIterator;

    public InternalTableIterator(TableIterator tableIterator) {
        this.tableIterator = tableIterator;
    }

    @Override
    protected void seekToFirstInternal() {
        tableIterator.seekToFirst();
    }

    @Override
    public void seekInternal(InternalKey targetKey) {
        tableIterator.seek(targetKey.encode());
    }

    @Override
    protected Entry<InternalKey, Slice> getNextElement() {
        if (tableIterator.hasNext()) {
            Entry<Slice, Slice> next = tableIterator.next();
            return Maps.immutableEntry(new InternalKey(next.getKey()), next.getValue());
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InternalTableIterator");
        sb.append("{fromIterator=").append(tableIterator);
        sb.append('}');
        return sb.toString();
    }
}
