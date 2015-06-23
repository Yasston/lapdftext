# Introduction #

The system uses rule files to improve performance. This page describes how rule files are developed and will provide examples for use in the future.

# Details #

Starting with version 1.6 we have improved the mechanism to specify input rules for the classification of blocks. You can now enter rules are drools decision tables specified in an Excel spreadsheet.
See [downloads](http://code.google.com/p/lapdftext/downloads/detail?name=epoch_7Jun_8.csv&can=2&q=) for an example.

Below is a snippet from a rule file included in the software distribution. The rules are in DROOLS syntax. http://en.wikipedia.org/wiki/Drools. The following snippet is taken from http://code.google.com/p/lapdftext/downloads/detail?name=epoch_7Jun_8.drl&can=2&q=. The rules in this file are meant specifically for a particular chronological range of documents with the PLoS biology journal. More specifically it is meant for all articles published in Volume 7 beginning in the month of June through to the end of Volume 8.

```
package edu.isi.bmkeg.pdf.classification.rules

import edu.isi.bmkeg.pdf.features.ChunkFeatures;
import edu.isi.bmkeg.pdf.model.ChunkBlock;

global ChunkBlock    chunk;

rule "Title"
	activation-group "blockClassification"
	salience 4
	when
		    ChunkFeatures(pageNumber==1)
	            ChunkFeatures(mostPopularFontSize==20)
		  
		    eval(chunk.getNumberOfLine()<=6)
		    ChunkFeatures(allignedMiddle==true)
		   
	then 
		chunk.setType(chunk.TYPE_TITLE);
		
end

rule "Authors"
	activation-group "blockClassification"
	salience 4
	when
		    ChunkFeatures(pageNumber==1)
		     ChunkFeatures(mostPopularFontSize==10)
		   
		    eval(chunk.getNumberOfLine()<=6)
		    ChunkFeatures(allignedMiddle==true)
		
		    features:ChunkFeatures()
		    eval(features.isMatchingRegularExpression("^(Summ|[Aa]bst|SUMM|ABST)")==false)
	then 
		chunk.setType(chunk.TYPE_AUTHORS);
		
end

rule "Header"
activation-group "blockClassification"
salience 4
	when
		
		 eval(chunk.getNumberOfLine()==1)
		 ChunkFeatures(containingFirstLineOfPage==true)
	then
		 chunk.setType(chunk.TYPE_HEADER);
end
```

From this example it is clear that LA-PDFText's code base contains a class that encapsulates features of chunk blocks detected in the block detection process. Here is a comprehensive list of ChunkFeatures you can use as antecedents of your drools rules.


  * `int	getChunkTextLength() `
> > returns the length of the text within a chunk
  * `double	getDensity() `
> > returns the word block density in a chunk block
  * `int	getHeightDifferenceBetweenChunkWordAndDocumentWord() `
> > returns the difference between the most popular font size in the in the current chunk and the most popular font size in the document.
  * `java.lang.String	getlastClassification() `
> > returns the classification assigned to previous chunk block
  * `int	getMostPopularFontSize() `
> > returns the most popular font size in the chunk block
  * `int	getPageNumber() `
> > returns the page number where the block is located
  * `java.lang.String	getSection() `
> > returns the section lable of chunk
  * `boolean	hasNeighboursOfType(java.lang.String type, int nsew) `
> > returns true if chunk block has neighbors of specific type within specified distance
  * `boolean	isAllCapitals() `
> > returns true if chunk block contains mostly capitalized text
  * `boolean	isAllignedLeft() `
> > returns true if chunk block is left aligned
  * `boolean	isAllignedMiddle() `
> > returns true if chunk block is center aligned
  * `boolean	isAllignedRight() `
> > returns true if chunk block is right aligned
  * `boolean	isAllignedWithColumnBoundaries() `
> > returns true if the chunk block is aligned with column boundaries
  * `boolean	isColumnCentered() `
> > returns true if the chunk is a sinle column centered on the page else returns false
  * `boolean	isContainingFirstLineOfPage() `
> > returns true if chunk block contains the first line of a page's text
  * `boolean	isContainingLastLineOfPage() `
> > returns true if chunk block contains the last line of a page's text
  * `boolean	isMatchingRegularExpression(java.lang.String regex) `
> > returns true if the chunk block contains text that matches the input regex
  * `boolean	isMostPopularFontModifierBold() `
> > returns true if chunk block contains mostly bold face text
  * `boolean	isMostPopularFontModifierItalic() `
> > returns true if chunk block contains mostly italicized text
  * `boolean	isOutlier() `
> > returns true if chunk block is an outlier or stray block
