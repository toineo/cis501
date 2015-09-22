package cis501.submission;

import cis501.IDirectionPredictor;

public class DirPredTournament extends DirPredBimodal {

    public DirPredTournament(int chooserIndexBits, IDirectionPredictor predictorNT, IDirectionPredictor predictorT) {
        super(chooserIndexBits); // re-use DirPredBimodal as the chooser table
    }

}
