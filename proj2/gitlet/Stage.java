package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.*;

public class Stage {
    private String stageName;
    private Map<String, String> stageBlobMap;
    private List<String> stageBlobID;

    public Stage(String stageName) {
        this.stageName = stageName;
    }
    
}
