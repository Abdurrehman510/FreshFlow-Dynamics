import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('pp_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// On 401 — clear token and redirect to login
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('pp_token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth ──────────────────────────────────────────────────────
export const authApi = {
  login:    (data) => api.post('/auth/login', data).then(r => r.data.data),
  register: (data) => api.post('/auth/register', data).then(r => r.data.data),
}

// ── Products ──────────────────────────────────────────────────
export const productApi = {
  getAvailable: ()      => api.get('/products/available').then(r => r.data.data),
  getDeals:     ()      => api.get('/products/deals').then(r => r.data.data),
  search:       (q)     => api.get('/products/search', { params: { q } }).then(r => r.data.data),
  getAll:       ()      => api.get('/products/all').then(r => r.data.data),
  getExpiring:  (days=3)=> api.get('/products/expiring', { params: { withinDays: days }}).then(r => r.data.data),
  create:       (data)  => api.post('/products', data).then(r => r.data.data),
  update:       (id,d)  => api.put(`/products/${id}`, d).then(r => r.data.data),
  delete:       (id)    => api.delete(`/products/${id}`).then(r => r.data),
  reprice:      ()      => api.post('/products/reprice').then(r => r.data),
}

// ── Orders ────────────────────────────────────────────────────
export const orderApi = {
  place:    (data) => api.post('/orders', data).then(r => r.data.data),
  getMyOrders: () => api.get('/orders/my').then(r => r.data.data),
  getAll:      () => api.get('/orders').then(r => r.data.data),
}

// ── Users ─────────────────────────────────────────────────────
export const userApi = {
  getMe:     ()      => api.get('/users/me').then(r => r.data.data),
  topUp:     (amount)=> api.post('/users/me/wallet/topup', { amount }).then(r => r.data.data),
  getAll:    ()      => api.get('/users').then(r => r.data.data),
  deleteUser:(id)    => api.delete(`/users/${id}`).then(r => r.data),
}

// ── Analytics ─────────────────────────────────────────────────
export const analyticsApi = {
  getDashboard: ()          => api.get('/analytics/dashboard').then(r => r.data.data),
  getWastage:   (from, to)  => api.get('/analytics/wastage', { params: { from, to }}).then(r => r.data.data),
  getForecast:  ()          => api.get('/analytics/forecast').then(r => r.data.data),
  getSuppliers: (cat)       => api.get('/analytics/suppliers', cat ? { params: {category:cat} } : {}).then(r => r.data.data),
  addSupplier:  (data)      => api.post('/analytics/suppliers', data).then(r => r.data.data),
}

export default api
