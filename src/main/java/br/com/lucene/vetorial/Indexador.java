package br.com.lucene.vetorial;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
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
	private IndexWriter writer;

	// Biblioteca que extrai texto de diversos formatos conhecidos
	private Tika tika;

	public Indexador(File dirIndice) {
		this.dirIndice = dirIndice;
		this.configurarIndexador();
	}

	public Tika getTika() {
		if (tika == null) {
			tika = new Tika();
		}
		return tika;
	}

	public void configurarIndexador() {
		try {
			Util.esvaziarDiretorio(dirIndice);
			logger.info("Diretorio do índice: " + dirIndice.getAbsolutePath());
			dirIndiceEmMemoria = new SimpleFSDirectory(dirIndice);

			// A separação dos termos é feita através dos espaços em branco do texto
			analisador = new WhitespaceAnalyzer(Version.LUCENE_48);

			config = new IndexWriterConfig(Version.LUCENE_48, analisador);
			// Inicializa o IndexWriter para gravação
			writer = new IndexWriter(dirIndiceEmMemoria, config);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void indexarDiretorio(File dirBasePreparada, String sufixoAceito) {
		for (File arquivo : Util.listarArquivosDiretorio(dirBasePreparada, "", sufixoAceito)) {
			if (arquivo.isFile())
				indexarArquivo(arquivo);
			else
				indexarDiretorio(arquivo, sufixoAceito);
		}

		try {
			writer.commit();
//			writer.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void indexarArquivo(File arquivo) {
		StringBuffer msg = new StringBuffer();
		msg.append("Indexando o arquivo: ");
		msg.append(arquivo.getAbsoluteFile());
		msg.append(", ");
		msg.append(arquivo.length() / 1000);
		msg.append("kb");
		logger.info(msg);

		try {
			// Extrai o conteúdo do arquivo com o Tika
			String textoExtraido = getTika().parseToString(arquivo);

			// Monta um Document para indexação
			// Cada item indexado é um Document e contém uma coleção de campos
			Document documento = new Document();
			documento.add(new TextField("Caminho", arquivo.getAbsolutePath(), Store.YES));
			// Field.Store.YES: armazena uma cópia do texto no índice
			documento.add(new TextField("Texto", textoExtraido, Store.YES));

			// Adiciona o Document no índice
			// Este só estará disponível para consulta após o commit
			writer.addDocument(documento);

		} catch (Exception e) {
			logger.error(e);
		}
	}

}
