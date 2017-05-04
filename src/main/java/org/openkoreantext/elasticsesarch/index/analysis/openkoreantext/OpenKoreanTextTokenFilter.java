package org.openkoreantext.elasticsesarch.index.analysis.openkoreantext;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.IOException;
import java.util.List;

import static org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;

public abstract class OpenKoreanTextTokenFilter extends TokenFilter {

    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    protected int tokenIndex = 0;
    protected List<KoreanToken> tokensForInc = null;

    public OpenKoreanTextTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        if(!(input instanceof OpenKoreanTextTokenizer)) {
            return incrementToken();
        } else {
            if(tokensForInc == null) {
                OpenKoreanTextTokenizer tokenizer = (OpenKoreanTextTokenizer) input;
                tokenizer.prepareTokens();
                this.tokensForInc = JavaConverters.seqAsJavaList(perform(tokenizer.getTokens()));
            }

            if (this.tokensForInc == null || this.tokensForInc.isEmpty() || tokenIndex >= this.tokensForInc.size()) {
                return false;
            }

            setAttributes(this.tokensForInc.get(tokenIndex++));
            return true;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        initializeState();
    }

    protected abstract Seq<KoreanToken> perform(Seq<KoreanToken> tokens);

    private void setAttributes(KoreanToken token) {
        charTermAttribute.append(token.text());
        offsetAttribute.setOffset(token.offset(), token.offset() + token.length());
        typeAttribute.setType(token.pos().toString());
    }

    private void initializeState() {
        this.tokenIndex = 0;
        this.tokensForInc = null;
    }
}
