package database.sql;

import database.model.Search;
import database.model.TeiDocument;

import java.util.ArrayList;
import java.util.List;

public class LoggableSearchChain {
    protected List<TeiDocument> results = null;
    protected List<Search> searches = new ArrayList<>();
}
