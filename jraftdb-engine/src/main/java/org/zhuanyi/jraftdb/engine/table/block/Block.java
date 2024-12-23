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
package org.zhuanyi.jraftdb.engine.table.block;

import org.zhuanyi.jraftdb.engine.utils.slice.Slice;
import org.zhuanyi.jraftdb.engine.utils.slice.Slices;
import org.zhuanyi.jraftdb.engine.iterator.SeekingIterable;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.zhuanyi.common.SizeOf.SIZE_OF_INT;

/**
 * SST中的DataBlock结构
 */
public class Block
        implements SeekingIterable<Slice, Slice> {
    private final Slice block;
    private final Comparator<Slice> comparator;

    private final Slice data;
    private final Slice restartPositions;

    public Block(Slice block, Comparator<Slice> comparator) {
        requireNonNull(block, "block is null");
        checkArgument(block.length() >= SIZE_OF_INT, "Block is corrupt: size must be at least %s block", SIZE_OF_INT);
        requireNonNull(comparator, "comparator is null");

        block = block.slice();
        this.block = block;
        this.comparator = comparator;

        // Keys are prefix compressed.  Every once in a while the prefix compression is restarted and the full key is written.
        // These "restart" locations are written at the end of the file, so you can seek to key without having to read the
        // entire file sequentially.

        // key restart count is the last int of the block
        int restartCount = block.getInt(block.length() - SIZE_OF_INT);

        if (restartCount > 0) {
            // restarts are written at the end of the block
            int restartOffset = block.length() - (1 + restartCount) * SIZE_OF_INT;
            checkArgument(restartOffset < block.length() - SIZE_OF_INT, "Block is corrupt: restart offset count is greater than block size");
            restartPositions = block.slice(restartOffset, restartCount * SIZE_OF_INT);

            // data starts at 0 and extends to the restart index
            data = block.slice(0, restartOffset);
        } else {
            data = Slices.EMPTY_SLICE;
            restartPositions = Slices.EMPTY_SLICE;
        }
    }

    public long size() {
        return block.length();
    }

    @Override
    public BlockIterator iterator() {
        return new BlockIterator(data, restartPositions, comparator);
    }
}
