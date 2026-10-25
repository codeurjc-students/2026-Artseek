import type { CategoryDTO } from '../app/features/categories/model/CategoryDTO'
import type { CategoryTypeDTO } from '../app/features/categories/model/CategoryTypeDTO'

export const categoryTypes: CategoryTypeDTO[] = [
  { value: 'painting' },
  { value: 'digital art/3D' },
]

export const categories: CategoryDTO[] = [
  { id: 1, name: 'Watercolour', type: 'painting' },
  { id: 2, name: 'Oil painting', type: 'painting' },
]
