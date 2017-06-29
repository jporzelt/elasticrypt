package org.elasticsearch.index.store

import org.elasticsearch.client.Client
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.NodeEnvironment
import org.elasticsearch.index.{Index, IndexService}
import org.elasticsearch.index.settings.IndexSettings
import org.elasticsearch.index.store.fs.FsIndexStore
import org.elasticsearch.indices.store.IndicesStore

class EncryptedIndexStore @Inject() (index: Index,
                                     @IndexSettings indexSettings: Settings,
                                     indexService: IndexService,
                                     indicesStore: IndicesStore,
                                     nodeEnv: NodeEnvironment,
                                     client: Client)
  extends FsIndexStore(index, indexSettings, indexService, indicesStore, nodeEnv) {

  override def shardDirectory(): Class[_ <: DirectoryService] = classOf[EncryptedDirectoryService]
}