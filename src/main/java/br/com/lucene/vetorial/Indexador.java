package br.com.lucene.vetorial;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;

import br.com.lucene.Util;

public class Indexador {
	private static Logger logger = Logger.getLogger(Indexador.class);

	// Diretório de armazenamento do índice
	private File dirIndice;

	// Representa o diretório do índice em memória
	private Directory dirIndiceEmMemoria;

	// Responsável pelo pré-processamento do texto
	private Analyzer analisador;

	// Configura a criação do índice
	private IndexWriterConfig config;

	// Biblioteca que cria e mantém o índice
	private IndexWriter escritor;

	// Biblioteca que extrai texto de diversos formatos conhecidos
	private Tika tika;

	public Indexador(File dirIndice, Directory dirIndiceEmMemoria, Analyzer analisador) {
		this.dirIndice = dirIndice;
		this.dirIndiceEmMemoria = dirIndiceEmMemoria;
		this.analisador = analisador;
	}

	public Tika getTika() {
		if (tika == null) {
			tika = new Tika();
		}
		return tika;
	}

	public void indexarDiretorio(File dirBasePreparada, String sufixoAceito) {
		Util.esvaziarDiretorio(dirIndice);

		try {
			config = new IndexWriterConfig(Version.LUCENE_48, analisador);
			// Inicializa o IndexWriter para gravação
			escritor = new IndexWriter(dirIndiceEmMemoria, config);

			for (File arquivo : Util.listarArquivosDiretorio(dirBasePreparada, "", sufixoAceito)) {
				if (arquivo.isFile())
					indexarArquivo(arquivo);
			}

			escritor.commit();
			escritor.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void indexarArquivo(File arquivo) {
		// logger.info("Indexando o arquivo: " + arquivo.getAbsoluteFile());

		try {
			// Extrai o conteúdo do arquivo com o Tika
			String conteudoArquivo = getTika().parseToString(arquivo);

			// Monta um Document para indexação
			// Cada item indexado é um Document e contém uma coleção de campos
			// Field.Store.YES: armazena uma cópia do campo índice
			Document documento = new Document();
			documento.add(new TextField("pathArquivo", arquivo.getAbsolutePath(), Store.YES));
			documento.add(new TextField("autorArquivo", Util.getNomeAutor(arquivo), Store.YES));
			documento.add(new TextField("conteudoIndice", conteudoArquivo, Store.YES));

			// Adiciona o Document no índice
			// Este só estará disponível para consulta após o commit
			escritor.addDocument(documento);

		} catch (Exception e) {
			logger.error(e);
		}
	}

}
