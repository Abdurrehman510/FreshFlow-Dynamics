import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  user: null,
  token: localStorage.getItem('pp_token'),

  login: (authData) => {
    localStorage.setItem('pp_token', authData.token)
    set({ user: authData, token: authData.token })
  },

  logout: () => {
    localStorage.removeItem('pp_token')
    set({ user: null, token: null })
  },

  isAdmin: () => {
    const state = useAuthStore.getState()
    return state.user?.role === 'STORE_ADMIN' || state.user?.role === 'PLATFORM_ADMIN'
  }
}))
