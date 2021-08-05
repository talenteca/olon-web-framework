package olon.markdown
import java.io.{InputStreamReader, StringWriter}

/**
 * This is the Transformer that uses the other parsers to transform markdown into xhtml.
 * Mix this trait in if you want more control over the output (like switching verbatim xml on/off or using
 * different opening/closing tags for the output).
 */
trait Transformer {

    /**
     * Overwrite this method to return a custom decorator if you want modified output.
     */
    def deco():Decorator = Decorator

    private object lineTokenizer extends LineTokenizer {
        override def allowXmlBlocks() = Transformer.this.deco().allowVerbatimXml()
    }
    private object blockParser extends BlockParsers {
        override def deco() = Transformer.this.deco()
    }

    /**
     * This is the method that turns markdown source into xhtml.
     */
    def apply(s:String) = {
        //first, run the input through the line tokenizer
        val lineReader = lineTokenizer.tokenize(s)
        //then, run it through the block parser
        blockParser(lineReader)
    }
}

class SingleThreadedTransformer extends Transformer

/**
 * Simple Standalone Markdown transformer.
 * Use this if you simply want to transform a block of markdown without any special options.
 * val input:String = ...
 * val xhtml:String = new ActuariusTransformer()(input)
 *
 * Note that this markdown parser isn't inherently thread-safe, as Scala Parser Combinators aren't, so this
 * class instantiates a SingleThreadedTransformer for each thread.
 * You'll need to write your own pooled implementation if this isn't efficient for your usage.
 */
class ThreadLocalTransformer extends Transformer {

    private[this] val threadLocalTransformer = new ThreadLocal[SingleThreadedTransformer] {
        override def initialValue = new SingleThreadedTransformer
    }

    override def apply(s: String) = threadLocalTransformer.get()(s)
}
