package c0rnell.flexer;

import c0rnell.flexer.model.GenerateModel;

@GenerateModel
public class TestEntity {

    private Long id;

    private String username;

    @GenerateModel.Ignore
    private String password;
}
