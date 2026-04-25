import { test, expect } from '@playwright/test'

const CRM_API_URL = process.env.CRM_API_URL || 'http://localhost:8080'

test.describe('客户管理', () => {
  test('创建客户', async ({ page, request }) => {
    const createRes = await request.post(`${CRM_API_URL}/api/customers`, {
      data: {
        name: 'E2E测试客户',
        phone: '13800138000',
        email: 'e2e@test.com',
        company: '测试有限公司',
        contactPerson: '张三',
        status: 'ACTIVE',
      },
    })
    expect(createRes.ok()).toBeTruthy()
    const createBody = await createRes.json()
    expect(createBody.code).toBe('200')

    await page.goto('/customers')

    await expect(page.getByText('E2E测试客户')).toBeVisible({ timeout: 15000 })
  })

  test('查询客户', async ({ request }) => {
    const createRes = await request.post(`${CRM_API_URL}/api/customers`, {
      data: {
        name: '查询测试客户',
        phone: '13900139000',
        email: 'query@test.com',
        company: '查询测试公司',
        contactPerson: '李四',
        source: 'WEBSITE',
        level: 'NORMAL',
        status: 'ACTIVE',
      },
    })
    expect(createRes.ok()).toBeTruthy()
    const createBody = await createRes.json()
    const customerId = createBody.data.id

    const listRes = await request.get(`${CRM_API_URL}/api/customers`, {
      params: { name: '查询测试客户', page: 0, size: 10 },
    })
    expect(listRes.ok()).toBeTruthy()
    const listBody = await listRes.json()
    expect(listBody.data.content.length).toBeGreaterThanOrEqual(1)
    const found = listBody.data.content.find((c: any) => c.id === customerId)
    expect(found).toBeDefined()
    expect(found.name).toBe('查询测试客户')
    expect(found.phone).toBe('13900139000')

    const detailRes = await request.get(`${CRM_API_URL}/api/customers/${customerId}`)
    expect(detailRes.ok()).toBeTruthy()
    const detailBody = await detailRes.json()
    expect(detailBody.data.id).toBe(customerId)
    expect(detailBody.data.name).toBe('查询测试客户')
    expect(detailBody.data.source).toBe('WEBSITE')
    expect(detailBody.data.level).toBe('NORMAL')
  })
})
