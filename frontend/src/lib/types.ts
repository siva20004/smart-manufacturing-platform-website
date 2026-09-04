export type RoleName = 
  | 'ADMIN' 
  | 'IT_ENGINEER' 
  | 'SALES' 
  | 'ENGINEERING' 
  | 'PROCUREMENT' 
  | 'PRODUCTION' 
  | 'FINANCE' 
  | 'MANAGEMENT';

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: RoleName[];
  isActive: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
  username?: string;
  email?: string;
  roles?: string[];
  user?: User;
}

export interface ApiResponse<T> {
  success: boolean;
  timestamp: string;
  data: T;
  error?: {
    code: string;
    message: string;
    details?: any[];
  };
}

export interface Customer {
  id: string;
  customerCode: string;
  companyName: string;
  industry?: string;
  country: string;
  status: 'ACTIVE' | 'CREDIT_HOLD' | 'INACTIVE';
  creditLimit?: number;
  paymentTerms?: string;
  contactName?: string;
  contactEmail?: string;
  phone?: string;
  address?: string;
  createdAt: string;
}

export interface CustomerContact {
  id: string;
  customerId: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  jobTitle?: string;
  department?: string;
  isPrimary: boolean;
  isActive: boolean;
  createdAt: string;
}

export interface CrmOpportunity {
  id: string;
  opportunityCode: string;
  customerId: string;
  customerName?: string;
  primaryContactId?: string;
  contactName?: string;
  productId?: string;
  productName?: string;
  name: string;
  stage: 'PROSPECTING' | 'QUALIFICATION' | 'PROPOSAL' | 'NEGOTIATION' | 'CLOSED_WON' | 'CLOSED_LOST';
  estimatedValue: number;
  currency: string;
  probabilityPct: number;
  expectedCloseDate?: string;
  createdAt: string;
}

export interface ServiceRequest {
  id: string;
  ticketNumber: string;
  customerId: string;
  customerName?: string;
  productId?: string;
  productName?: string;
  machineSerialNo?: string;
  title: string;
  reportedIssue: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  assignedTechnicianId?: string;
  resolutionNotes?: string;
  resolvedAt?: string;
  createdAt: string;
}

export interface Product {
  id: string;
  productNumber: string;
  name: string;
  description?: string;
  category?: string;
  status: 'DRAFT' | 'ACTIVE' | 'OBSOLETE';
  standardCost?: number;
  listPrice?: number;
  createdAt: string;
}

export interface ProductDocument {
  id: string;
  productId: string;
  documentNumber: string;
  title: string;
  documentType: 'DRAWING' | 'CAD_MODEL' | 'MANUAL' | 'DATASHEET';
  mimeType: string;
  fileSizeBytes: number;
  storageKey: string;
  version: number;
  createdAt: string;
}

export interface EbomItem {
  id: string;
  itemSeq: number;
  partNumber: string;
  description: string;
  itemType: 'ASSEMBLY' | 'COMPONENT' | 'RAW_MATERIAL';
  quantity: number;
  uom: string;
  children?: EbomItem[];
}

export interface EbomHeader {
  id: string;
  productId: string;
  productNumber: string;
  productName: string;
  revisionCode: string;
  status: 'DRAFT' | 'IN_REVIEW' | 'APPROVED' | 'RELEASED';
  items?: EbomItem[];
  createdAt: string;
}

export interface MbomItem {
  id: string;
  itemSeq: number;
  partNumber: string;
  description: string;
  itemType: 'ASSEMBLY' | 'COMPONENT' | 'CONSUMABLE';
  quantity: number;
  uom: string;
  workCenter?: string;
  operationSeq?: number;
  operationName?: string;
}

export interface MbomHeader {
  id: string;
  ebomHeaderId: string;
  productId: string;
  productNumber: string;
  productName: string;
  revisionCode: string;
  plantLocation: string;
  status: 'DRAFT' | 'RELEASED';
  items?: MbomItem[];
  createdAt: string;
}

export interface Warehouse {
  id: string;
  code: string;
  name: string;
  plantLocation: string;
  isActive: boolean;
}

export interface InventoryStock {
  id: string;
  warehouseId: string;
  warehouseCode: string;
  partNumber: string;
  description?: string;
  qtyOnHand: number;
  qtyReserved: number;
  qtyAvailable: number;
  uom: string;
  unitCost: number;
}

export interface Quotation {
  id: string;
  quotationCode: string;
  revisionLetter: string;
  customerId: string;
  customerName: string;
  status: string;
  subtotalAmount: number;
  taxAmount: number;
  totalAmount: number;
  currency: string;
  validUntil?: string;
  items: Array<{
    id: string;
    itemSeq: number;
    productId: string;
    productNumber: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
  }>;
  createdAt: string;
}

export interface SalesOrder {
  id: string;
  soCode: string;
  quotationId?: string;
  customerId: string;
  customerName: string;
  customerPoNumber?: string;
  orderDate: string;
  requestedDeliveryDate?: string;
  plantLocation: string;
  status: 'DRAFT' | 'CONFIRMED' | 'IN_PRODUCTION' | 'READY_TO_SHIP' | 'SHIPPED' | 'CANCELLED';
  subtotalAmount: number;
  taxAmount: number;
  totalAmount: number;
  currency: string;
  items: Array<{
    id: string;
    itemSeq: number;
    productId: string;
    productNumber: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
    status: string;
  }>;
  createdAt: string;
}

export interface Supplier {
  id: string;
  supplierCode: string;
  companyName: string;
  taxId?: string;
  country: string;
  contactEmail?: string;
  phone?: string;
  address?: string;
  paymentTerms: string;
  status: 'ACTIVE' | 'ON_HOLD' | 'BLACKLISTED';
  createdAt: string;
}

export interface PurchaseRequest {
  id: string;
  prCode: string;
  requestedBy?: string;
  sourceType: string;
  status: 'DRAFT' | 'APPROVED' | 'CONVERTED_TO_PO' | 'REJECTED';
  totalEstimatedAmount: number;
  items: Array<{
    id: string;
    itemSeq: number;
    partNumber: string;
    description: string;
    quantityRequested: number;
    uom: string;
    estimatedUnitPrice: number;
  }>;
  createdAt: string;
}

export interface PurchaseOrder {
  id: string;
  poCode: string;
  supplierId: string;
  supplierName: string;
  warehouseId: string;
  warehouseCode: string;
  orderDate: string;
  expectedDeliveryDate?: string;
  status: 'DRAFT' | 'ISSUED' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED';
  subtotalAmount: number;
  taxAmount: number;
  totalAmount: number;
  currency: string;
  items: Array<{
    id: string;
    itemSeq: number;
    partNumber: string;
    description: string;
    quantityOrdered: number;
    quantityReceived: number;
    uom: string;
    unitPrice: number;
    lineTotal: number;
    status: string;
  }>;
  createdAt: string;
}

export interface ProductionOperation {
  id: string;
  operationSeq: number;
  operationName: string;
  workCenterCode: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';
  plannedHours?: number;
  actualHours?: number;
  startedAt?: string;
  completedAt?: string;
}

export interface ProductionOrder {
  id: string;
  orderCode: string;
  salesOrderId?: string;
  productId: string;
  productNumber: string;
  productName: string;
  mbomHeaderId?: string;
  mbomRevision?: string;
  warehouseId: string;
  warehouseCode: string;
  plantLocation: string;
  quantityPlanned: number;
  quantityCompleted: number;
  plannedStartDate?: string;
  plannedCompletionDate?: string;
  actualStartDate?: string;
  actualCompletionDate?: string;
  status: 'PLANNED' | 'MATERIAL_RESERVED' | 'IN_PROGRESS' | 'QUALITY_CHECK' | 'COMPLETED' | 'CANCELLED';
  operations: ProductionOperation[];
  createdAt: string;
}

export interface AuditLog {
  id: string;
  entityName: string;
  entityId: string;
  action: string;
  changedData?: string;
  userId?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface AnalyticsSummary {
  salesByCustomer: Array<{
    customerId: string;
    customerCode: string;
    companyName: string;
    totalOrders: number;
    totalRevenue: number;
  }>;
  salesByProduct: Array<{
    productId: string;
    productNumber: string;
    productName: string;
    totalQuantitySold: number;
    totalRevenue: number;
  }>;
  orderVolume: Array<{
    orderStatus: string;
    orderCount: number;
    totalAmount: number;
  }>;
  inventoryValue: Array<{
    warehouseId: string;
    warehouseCode: string;
    plantLocation: string;
    uniquePartsCount: number;
    totalInventoryValue: number;
  }>;
  productionMetrics: {
    totalOrdersPlanned: number;
    totalOrdersCompleted: number;
    activeOrdersInProgress: number;
    totalUnitsProduced: number;
    onTimeCompletionRatePct: number;
  };
  supplierPerformance: Array<{
    supplierId: string;
    supplierCode: string;
    companyName: string;
    totalPurchaseOrders: number;
    totalSpend: number;
    totalUnitsOrdered: number;
    totalUnitsReceived: number;
  }>;
  serviceMetrics: {
    totalTickets: number;
    openTickets: number;
    inProgressTickets: number;
    resolvedTickets: number;
    criticalTickets: number;
  };
}
