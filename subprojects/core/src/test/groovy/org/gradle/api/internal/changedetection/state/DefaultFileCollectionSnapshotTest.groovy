/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.changedetection.state

import org.gradle.caching.internal.BuildCacheHasher
import org.gradle.caching.internal.DefaultBuildCacheHasher
import org.gradle.internal.hash.HashCode
import spock.lang.Specification

import static org.gradle.api.internal.changedetection.state.TaskFilePropertyCompareStrategy.ORDERED
import static org.gradle.api.internal.changedetection.state.TaskFilePropertyCompareStrategy.UNORDERED

class DefaultFileCollectionSnapshotTest extends Specification {

    def "order-insensitive collection snapshot ignores order when hashing"() {
        def hasher = Mock(BuildCacheHasher)
        def oldSnapshot = new DefaultFileCollectionSnapshot([
            "file1.txt": new DefaultNormalizedFileSnapshot("file1.txt", new FileHashSnapshot(HashCode.fromInt(123))),
            "file2.txt": new DefaultNormalizedFileSnapshot("file2.txt", new FileHashSnapshot(HashCode.fromInt(234))),
        ], UNORDERED, false)
        def newSnapshot = new DefaultFileCollectionSnapshot([
            "file2.txt": new DefaultNormalizedFileSnapshot("file2.txt", new FileHashSnapshot(HashCode.fromInt(234))),
            "file1.txt": new DefaultNormalizedFileSnapshot("file1.txt", new FileHashSnapshot(HashCode.fromInt(123))),
        ], UNORDERED, false)

        when:
        oldSnapshot.appendToHasher(hasher)

        then:
        1 * hasher.putHash(new DefaultBuildCacheHasher()
            .putString("file1.txt")
            .putHash(HashCode.fromInt(123))
            .putString("file2.txt")
            .putHash(HashCode.fromInt(234))
            .hash())
        0 * _

        when:
        newSnapshot.appendToHasher(hasher)
        then:
        1 * hasher.putHash(new DefaultBuildCacheHasher()
            .putString("file1.txt")
            .putHash(HashCode.fromInt(123))
            .putString("file2.txt")
            .putHash(HashCode.fromInt(234))
            .hash())
        0 * _
    }

    def "order-sensitive collection snapshot considers order when hashing"() {
        def hasher = Mock(BuildCacheHasher)
        def oldSnapshot = new DefaultFileCollectionSnapshot([
            "file1.txt": new DefaultNormalizedFileSnapshot("file1.txt", new FileHashSnapshot(HashCode.fromInt(123))),
            "file2.txt": new DefaultNormalizedFileSnapshot("file2.txt", new FileHashSnapshot(HashCode.fromInt(234))),
        ], ORDERED, false)
        def newSnapshot = new DefaultFileCollectionSnapshot([
            "file2.txt": new DefaultNormalizedFileSnapshot("file2.txt", new FileHashSnapshot(HashCode.fromInt(234))),
            "file1.txt": new DefaultNormalizedFileSnapshot("file1.txt", new FileHashSnapshot(HashCode.fromInt(123))),
        ], ORDERED, false)
        when:
        oldSnapshot.appendToHasher(hasher)
        then:
        1 * hasher.putHash(new DefaultBuildCacheHasher()
            .putString("file1.txt")
            .putHash(HashCode.fromInt(123))
            .putString("file2.txt")
            .putHash(HashCode.fromInt(234))
            .hash())
        0 * _

        when:
        newSnapshot.appendToHasher(hasher)
        then:
        1 * hasher.putHash(new DefaultBuildCacheHasher()
            .putString("file2.txt")
            .putHash(HashCode.fromInt(234))
            .putString("file1.txt")
            .putHash(HashCode.fromInt(123))
            .hash())
        0 * _
    }
}
