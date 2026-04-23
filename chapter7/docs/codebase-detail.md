# crm-frontend 全量文件列表
```
crm-frontend/
├── README.md
├── components.d.ts
├── dist/
│   ├── favicon.svg
│   ├── icons.svg
│   └── index.html
├── index.html
├── package-lock.json
├── package.json
├── public/
│   ├── favicon.svg
│   └── icons.svg
├── src/
│   ├── App.vue
│   ├── api/
│   │   ├── customer.ts
│   │   ├── customerNote.ts
│   │   ├── lead.ts
│   │   ├── opportunity.ts
│   │   ├── order.ts
│   │   ├── request.ts
│   │   └── tag.ts
│   ├── assets/
│   │   ├── hero.png
│   │   ├── vite.svg
│   │   └── vue.svg
│   ├── components/
│   │   ├── AppLayout.vue
│   │   └── TagSelect.vue
│   ├── main.ts
│   ├── router/
│   │   └── index.ts
│   ├── stores/
│   │   ├── customer.ts
│   │   ├── customerNote.ts
│   │   ├── index.ts
│   │   ├── lead.ts
│   │   ├── opportunity.ts
│   │   ├── order.ts
│   │   └── tag.ts
│   ├── types/
│   │   ├── common.ts
│   │   ├── customer.ts
│   │   ├── lead.ts
│   │   ├── opportunity.ts
│   │   ├── order.ts
│   │   └── tag.ts
│   ├── utils/
│   └── views/
│       ├── customer/
│       │   ├── CustomerDetail.vue
│       │   ├── CustomerForm.vue
│       │   └── CustomerList.vue
│       ├── lead/
│       │   ├── ConvertDialog.vue
│       │   ├── LeadForm.vue
│       │   └── LeadList.vue
│       ├── opportunity/
│       │   ├── OppDetail.vue
│       │   ├── OppForm.vue
│       │   ├── OppKanban.vue
│       │   └── OppList.vue
│       ├── order/
│       │   ├── OrderDetail.vue
│       │   ├── OrderForm.vue
│       │   └── OrderList.vue
│       └── tag/
│           └── TagList.vue
├── tsconfig.app.json
├── tsconfig.json
├── tsconfig.node.json
└── vite.config.ts
```

# crm-backend 全量文件列表
```
crm-backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/harness/crm/
    │   │   ├── CrmApplication.java
    │   │   ├── adapter/web/
    │   │   │   ├── CustomerController.java
    │   │   │   ├── LeadController.java
    │   │   │   ├── OpportunityController.java
    │   │   │   ├── OrderController.java
    │   │   │   ├── TagController.java
    │   │   │   └── common/
    │   │   │       ├── ApiResponse.java
    │   │   │       └── GlobalExceptionHandler.java
    │   │   ├── app/
    │   │   │   ├── customer/
    │   │   │   │   ├── CustomerNoteService.java
    │   │   │   │   ├── CustomerService.java
    │   │   │   │   ├── TagService.java
    │   │   │   │   └── dto/
    │   │   │   │       ├── CustomerDTO.java
    │   │   │   │       ├── CustomerNoteDTO.java
    │   │   │   │       └── TagDTO.java
    │   │   │   ├── lead/
    │   │   │   │   ├── LeadService.java
    │   │   │   │   └── dto/
    │   │   │   │       ├── LeadConvertDTO.java
    │   │   │   │       └── LeadDTO.java
    │   │   │   ├── opportunity/
    │   │   │   │   ├── OpportunityService.java
    │   │   │   │   └── dto/
    │   │   │   │       ├── OppWinDTO.java
    │   │   │   │       └── OpportunityDTO.java
    │   │   │   └── order/
    │   │   │       ├── OrderService.java
    │   │   │       └── dto/
    │   │   │           └── OrderDTO.java
    │   │   ├── domain/
    │   │   │   ├── customer/
    │   │   │   │   ├── entity/
    │   │   │   │   │   ├── CustomerEntity.java
    │   │   │   │   │   ├── CustomerNoteEntity.java
    │   │   │   │   │   ├── CustomerTagRelEntity.java
    │   │   │   │   │   └── TagEntity.java
    │   │   │   │   ├── enums/
    │   │   │   │   │   ├── CustomerLevel.java
    │   │   │   │   │   ├── CustomerSource.java
    │   │   │   │   │   ├── CustomerStatus.java
    │   │   │   │   │   └── NoteCategory.java
    │   │   │   │   ├── gateway/
    │   │   │   │   │   ├── CustomerGatewayI.java
    │   │   │   │   │   ├── CustomerNoteGatewayI.java
    │   │   │   │   │   ├── CustomerTagGatewayI.java
    │   │   │   │   │   └── TagGatewayI.java
    │   │   │   │   └── service/
    │   │   │   ├── lead/
    │   │   │   │   ├── entity/
    │   │   │   │   │   └── LeadEntity.java
    │   │   │   │   ├── enums/
    │   │   │   │   │   └── LeadStatus.java
    │   │   │   │   ├── gateway/
    │   │   │   │   │   └── LeadGatewayI.java
    │   │   │   │   └── service/
    │   │   │   ├── opportunity/
    │   │   │   │   ├── entity/
    │   │   │   │   │   └── OpportunityEntity.java
    │   │   │   │   ├── enums/
    │   │   │   │   │   └── OppStage.java
    │   │   │   │   ├── gateway/
    │   │   │   │   │   └── OpportunityGatewayI.java
    │   │   │   │   └── service/
    │   │   │   └── order/
    │   │   │       ├── entity/
    │   │   │       │   └── OrderEntity.java
    │   │   │       ├── enums/
    │   │   │       │   └── OrderStatus.java
    │   │   │       ├── gateway/
    │   │   │       │   └── OrderGatewayI.java
    │   │   │       └── service/
    │   │   └── infrastructure/
    │   │       ├── customer/
    │   │       │   ├── gateway/
    │   │       │   │   ├── CustomerGatewayImpl.java
    │   │       │   │   ├── CustomerNoteGatewayImpl.java
    │   │       │   │   ├── CustomerTagGatewayImpl.java
    │   │       │   │   └── TagGatewayImpl.java
    │   │       │   └── repository/
    │   │       │       ├── CustomerNoteRepository.java
    │   │       │       ├── CustomerRepository.java
    │   │       │       ├── CustomerTagRelRepository.java
    │   │       │       └── TagRepository.java
    │   │       ├── lead/
    │   │       │   ├── gateway/
    │   │       │   │   └── LeadGatewayImpl.java
    │   │       │   └── repository/
    │   │       │       └── LeadRepository.java
    │   │       ├── opportunity/
    │   │       │   ├── gateway/
    │   │       │   │   └── OpportunityGatewayImpl.java
    │   │       │   └── repository/
    │   │       │       └── OpportunityRepository.java
    │   │       └── order/
    │   │           ├── gateway/
    │   │           │   └── OrderGatewayImpl.java
    │   │           └── repository/
    │   │               └── OrderRepository.java
    │   └── resources/
    │       ├── application-dev.yml
    │       ├── application.yml
    │       └── db/
    │           └── schema.sql
    └── test/
        ├── java/com/harness/crm/
        │   ├── ColaArchitectureTest.java
        │   ├── adapter/web/
        │   │   ├── CustomerControllerIntegrationTest.java
        │   │   ├── CustomerNoteControllerIntegrationTest.java
        │   │   ├── LeadControllerIntegrationTest.java
        │   │   ├── OpportunityControllerIntegrationTest.java
        │   │   ├── OrderControllerIntegrationTest.java
        │   │   └── TagControllerIntegrationTest.java
        │   ├── app/
        │   │   ├── lead/
        │   │   ├── opportunity/
        │   │   └── order/
        │   └── domain/
        │       ├── lead/service/
        │       ├── opportunity/service/
        │       └── order/service/
        └── resources/
            └── application-test.yml
```